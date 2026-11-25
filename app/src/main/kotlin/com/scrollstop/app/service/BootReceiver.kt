package com.scrollstop.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.scrollstop.app.MainActivity

/**
 * Receives BOOT_COMPLETED and reminds the user to re-enable the
 * accessibility service if ScrollStop was running before restart.
 * Android requires the user to manually re-enable accessibility services
 * after every device reboot — we cannot re-enable it automatically.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "scrollstop_boot"
        private const val NOTIFICATION_ID = 2001
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        createChannel(context)
        showReminder(context)
    }

    private fun createChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "ReelStoper Boot Reminder",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nm.createNotificationChannel(channel)
    }

    private fun showReminder(context: Context) {
        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("ReelStoper needs your attention")
            .setContentText("Tap to re-enable the accessibility service after restart.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }
}
