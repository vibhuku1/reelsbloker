package com.scrollstop.app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scrollstop.app.data.repository.ScrollStopRepository
import com.scrollstop.app.model.AppSettings
import com.scrollstop.app.model.BlockEvent
import com.scrollstop.app.model.Statistics
import com.scrollstop.app.model.ThemeMode
import com.scrollstop.app.service.ServiceStateHolder
import com.scrollstop.app.utils.AccessibilityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val settings: AppSettings = AppSettings(),
    val statistics: Statistics = Statistics(),
    val recentEvents: List<BlockEvent> = emptyList(),
    val isAccessibilityEnabled: Boolean = false,
    val isServiceRunning: Boolean = false,
    val youtubeInstalled: Boolean = false,
    val instagramInstalled: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScrollStopRepository(application)
    private val context: Context get() = getApplication()

    private val _accessibilityEnabled = MutableStateFlow(false)
    private val _youtubeInstalled = MutableStateFlow(false)
    private val _instagramInstalled = MutableStateFlow(false)

    // combine only supports up to 5 flows in one call natively.
    // Chain two combines to handle 7 flows cleanly without unchecked casts.
    private val appInfoFlow = combine(
        _accessibilityEnabled,
        ServiceStateHolder.isRunning,
        _youtubeInstalled,
        _instagramInstalled
    ) { a11y, svcRunning, yt, ig ->
        listOf(a11y, svcRunning, yt, ig)
    }

    val uiState: StateFlow<MainUiState> = combine(
        repository.settingsFlow,
        repository.getStatisticsFlow(),
        repository.getAllEventsFlow(),
        appInfoFlow
    ) { settings, statistics, events, appInfo ->
        val a11y = appInfo[0] as Boolean
        val svcRunning = appInfo[1] as Boolean
        val yt = appInfo[2] as Boolean
        val ig = appInfo[3] as Boolean
        MainUiState(
            settings = settings,
            statistics = statistics,
            recentEvents = events.take(50),
            isAccessibilityEnabled = a11y,
            isServiceRunning = svcRunning,
            youtubeInstalled = yt,
            instagramInstalled = ig
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )

    init {
        refreshAccessibilityState()
    }

    fun refreshAccessibilityState() {
        _accessibilityEnabled.value = AccessibilityUtils.isAccessibilityServiceEnabled(context)
        _youtubeInstalled.value = AccessibilityUtils.isAppInstalled(context, "com.google.android.youtube")
        _instagramInstalled.value = AccessibilityUtils.isAppInstalled(context, "com.instagram.android")
    }

    fun setEnabled(enabled: Boolean) = viewModelScope.launch {
        repository.setEnabled(enabled)
    }

    fun setBlockShorts(block: Boolean) = viewModelScope.launch {
        repository.setBlockShorts(block)
    }

    fun setBlockReels(block: Boolean) = viewModelScope.launch {
        repository.setBlockReels(block)
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        repository.setThemeMode(mode)
    }

    fun setOnboardingDone(done: Boolean) = viewModelScope.launch {
        repository.setOnboardingDone(done)
    }

    fun clearAllData() = viewModelScope.launch {
        repository.clearAllData()
    }

    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        repository.setNotificationsEnabled(enabled)
    }
}
