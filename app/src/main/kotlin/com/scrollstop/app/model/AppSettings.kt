package com.scrollstop.app.model

data class AppSettings(
    val isEnabled: Boolean = true,
    val blockYouTubeShorts: Boolean = true,
    val blockInstagramReels: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val hasCompletedOnboarding: Boolean = false,
    val notificationsEnabled: Boolean = true
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}
