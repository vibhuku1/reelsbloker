package com.scrollstop.app

import com.scrollstop.app.model.AppSettings
import com.scrollstop.app.model.BlockEvent
import com.scrollstop.app.model.ContentType
import com.scrollstop.app.model.Statistics
import com.scrollstop.app.model.ThemeMode
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for data models.
 */
class ModelTest {

    @Test
    fun `Statistics totalBlocked is sum of shorts and reels`() {
        val stats = Statistics(
            totalShortsBlocked = 5,
            totalReelsBlocked = 3
        )
        assertEquals(8, stats.totalBlocked)
    }

    @Test
    fun `Statistics todayBlocked is sum of today shorts and reels`() {
        val stats = Statistics(
            todayShortsBlocked = 2,
            todayReelsBlocked = 4
        )
        assertEquals(6, stats.todayBlocked)
    }

    @Test
    fun `AppSettings defaults are correct`() {
        val settings = AppSettings()
        assertTrue(settings.isEnabled)
        assertTrue(settings.blockYouTubeShorts)
        assertTrue(settings.blockInstagramReels)
        assertEquals(ThemeMode.DARK, settings.themeMode)
        assertFalse(settings.hasCompletedOnboarding)
    }

    @Test
    fun `ContentType displayName is human readable`() {
        assertEquals("YouTube Shorts", ContentType.YOUTUBE_SHORTS.displayName)
        assertEquals("Instagram Reels", ContentType.INSTAGRAM_REELS.displayName)
    }

    @Test
    fun `BlockEvent defaults timestamp to current time`() {
        val before = System.currentTimeMillis()
        val event = BlockEvent(
            appPackage = "com.google.android.youtube",
            contentType = ContentType.YOUTUBE_SHORTS
        )
        val after = System.currentTimeMillis()
        assertTrue(event.timestamp in before..after)
    }

    @Test
    fun `Statistics estimatedMinutesSaved increases with more blocks`() {
        val statsLow = Statistics(totalShortsBlocked = 2, totalReelsBlocked = 1)
        val statsHigh = Statistics(totalShortsBlocked = 10, totalReelsBlocked = 10)
        assertTrue(statsHigh.estimatedMinutesSaved > statsLow.estimatedMinutesSaved)
    }
}
