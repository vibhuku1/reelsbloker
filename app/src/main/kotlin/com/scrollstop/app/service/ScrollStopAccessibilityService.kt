package com.scrollstop.app.service

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import com.scrollstop.app.MainActivity
import com.scrollstop.app.data.repository.ScrollStopRepository
import com.scrollstop.app.detection.DetectionResult
import com.scrollstop.app.detection.InstagramReelsDetector
import com.scrollstop.app.detection.YouTubeShortsDetector
import com.scrollstop.app.model.AppSettings
import com.scrollstop.app.model.BlockEvent
import com.scrollstop.app.model.ContentType
import com.scrollstop.app.ui.screens.BlockingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ScrollStopAccessibilityService : AccessibilityService() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "scrollstop_service"
        const val NOTIFICATION_ID = 1001
        const val BLOCK_NOTIFICATION_ID = 1002

        // Cooldown between successive blocks of the same type (ms)
        private const val BLOCK_COOLDOWN_MS = 3000L

        // Extra intent keys passed to BlockingActivity
        const val EXTRA_CONTENT_TYPE = "content_type"
        const val EXTRA_APP_PACKAGE = "app_package"
    }

    // Use Default dispatcher for all coroutine work; switch to Main where needed
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var repository: ScrollStopRepository
    private val youtubeShortsDetector = YouTubeShortsDetector()
    private val instagramReelsDetector = InstagramReelsDetector()

    private var currentSettings = AppSettings()
    private var settingsJob: Job? = null

    // De-duplication: track the last blocked content type and timestamp
    private var lastBlockedType: ContentType? = null
    private var lastBlockedTime: Long = 0L

    // Handler for performing back action on main thread
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        repository = ScrollStopRepository(applicationContext)
        createNotificationChannel()
        // Post a persistent notification via NotificationManager (not startForeground —
        // AccessibilityService is not a started service so startForeground is not valid here)
        showServiceNotification()
        observeSettings()
        ServiceStateHolder.setServiceRunning(true)
    }

    private fun observeSettings() {
        settingsJob?.cancel()
        settingsJob = serviceScope.launch {
            repository.settingsFlow.collect { settings ->
                currentSettings = settings
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        // Only react to window state changes
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) return

        // Guard: service globally disabled
        if (!currentSettings.isEnabled) return

        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: ""

        // Only process YouTube or Instagram
        if (packageName != YouTubeShortsDetector.YOUTUBE_PACKAGE &&
            packageName != InstagramReelsDetector.INSTAGRAM_PACKAGE
        ) return

        // Snapshot the accessibility tree on the calling thread (accessibility thread)
        // before dispatching to the coroutine, since rootInActiveWindow is only valid now.
        val root: AccessibilityNodeInfo? = try {
            rootInActiveWindow
        } catch (e: Exception) {
            null
        }

        serviceScope.launch {
            processEvent(packageName, className, root)
        }
    }

    private suspend fun processEvent(
        packageName: String,
        className: String,
        root: AccessibilityNodeInfo?
    ) {
        val result: DetectionResult = when (packageName) {
            YouTubeShortsDetector.YOUTUBE_PACKAGE -> {
                if (!currentSettings.blockYouTubeShorts) return
                youtubeShortsDetector.detect(packageName, className, root)
            }
            InstagramReelsDetector.INSTAGRAM_PACKAGE -> {
                if (!currentSettings.blockInstagramReels) return
                instagramReelsDetector.detect(packageName, className, root)
            }
            else -> return
        }

        if (result is DetectionResult.Detected) {
            handleDetection(result, packageName)
        }
    }

    private suspend fun handleDetection(
        result: DetectionResult.Detected,
        packageName: String
    ) {
        val now = System.currentTimeMillis()

        // Cooldown check: prevent repeated triggers for the same content
        if (result.contentType == lastBlockedType && (now - lastBlockedTime) < BLOCK_COOLDOWN_MS) {
            return
        }

        lastBlockedType = result.contentType
        lastBlockedTime = now

        // Record only minimal non-sensitive metadata (no UI text/signals stored)
        repository.recordBlockEvent(
            BlockEvent(
                timestamp = now,
                appPackage = packageName,
                contentType = result.contentType,
                detectionSignals = ""
            )
        )

        // Show block notification
        if (currentSettings.notificationsEnabled) {
            showBlockNotification(result.contentType)
        }

        // Navigate back on the main thread
        mainHandler.post {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }

        // Launch BlockingActivity shortly after the back action completes
        mainHandler.postDelayed({
            launchBlockingActivity(packageName, result.contentType)
        }, 200)
    }

    private fun launchBlockingActivity(packageName: String, contentType: ContentType) {
        val intent = Intent(applicationContext, BlockingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CONTENT_TYPE, contentType.name)
            putExtra(EXTRA_APP_PACKAGE, packageName)
        }
        try {
            applicationContext.startActivity(intent)
        } catch (e: Exception) {
            // Swallow — activity launch can fail if the app is backgrounded
        }
    }

    private fun showBlockNotification(contentType: ContentType) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("ScrollStop – Blocked!")
            .setContentText("${contentType.displayName} blocked. Stay focused! \uD83D\uDCAA")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(BLOCK_NOTIFICATION_ID, notification)
    }

    private fun showServiceNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("ScrollStop is active")
            .setContentText("Protecting you from Shorts & Reels")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "ScrollStop Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "ScrollStop accessibility service status"
            setShowBadge(false)
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onInterrupt() {
        ServiceStateHolder.setServiceRunning(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        // Dismiss the persistent notification
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        ServiceStateHolder.setServiceRunning(false)
    }
}
