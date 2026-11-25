package com.scrollstop.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.scrollstop.app.model.AppSettings
import com.scrollstop.app.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scroll_stop_prefs")

class SettingsDataStore(private val context: Context) {

    companion object {
        val KEY_ENABLED = booleanPreferencesKey("is_enabled")
        val KEY_BLOCK_SHORTS = booleanPreferencesKey("block_youtube_shorts")
        val KEY_BLOCK_REELS = booleanPreferencesKey("block_instagram_reels")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            isEnabled = prefs[KEY_ENABLED] ?: true,
            blockYouTubeShorts = prefs[KEY_BLOCK_SHORTS] ?: true,
            blockInstagramReels = prefs[KEY_BLOCK_REELS] ?: true,
            themeMode = prefs[KEY_THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrDefault(ThemeMode.DARK) } ?: ThemeMode.DARK,
            hasCompletedOnboarding = prefs[KEY_ONBOARDING_DONE] ?: false,
            notificationsEnabled = prefs[KEY_NOTIFICATIONS] ?: true
        )
    }

    suspend fun setEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_ENABLED] = enabled }
    }

    suspend fun setBlockShorts(block: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_BLOCK_SHORTS] = block }
    }

    suspend fun setBlockReels(block: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_BLOCK_REELS] = block }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs -> prefs[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_ONBOARDING_DONE] = done }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_NOTIFICATIONS] = enabled }
    }
}
