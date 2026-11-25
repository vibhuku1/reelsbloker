package com.scrollstop.app

import com.scrollstop.app.detection.DetectionResult
import com.scrollstop.app.detection.YouTubeShortsDetector
import com.scrollstop.app.detection.InstagramReelsDetector
import com.scrollstop.app.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the detection engine.
 * Run with: ./gradlew test
 *
 * Note: Tests involving real AccessibilityNodeInfo objects require instrumented tests
 * on a real device (see androidTest). These unit tests cover the pure logic paths.
 */
class DetectionEngineTest {

    private lateinit var youtubeDetector: YouTubeShortsDetector
    private lateinit var instagramDetector: InstagramReelsDetector

    @Before
    fun setup() {
        youtubeDetector = YouTubeShortsDetector()
        instagramDetector = InstagramReelsDetector()
    }

    // --- YouTube Shorts Detector ---

    @Test
    fun `youtube detector returns NotDetected for non-youtube package`() {
        val result = youtubeDetector.detect(
            packageName = "com.example.other",
            className = "com.example.other.MainActivity",
            root = null
        )
        assertTrue(result is DetectionResult.NotDetected)
    }

    @Test
    fun `youtube detector returns NotDetected for youtube package with no shorts signals`() {
        // Simulates opening a normal YouTube video screen
        val result = youtubeDetector.detect(
            packageName = YouTubeShortsDetector.YOUTUBE_PACKAGE,
            className = "com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity",
            root = null
        )
        // With no tree, only class signals matter – WatchWhile is not a shorts class
        assertTrue(result is DetectionResult.NotDetected)
    }

    @Test
    fun `youtube detector returns Detected for known Shorts class name`() {
        val result = youtubeDetector.detect(
            packageName = YouTubeShortsDetector.YOUTUBE_PACKAGE,
            className = "com.google.android.apps.youtube.app.shorts.ShortsPivotFragment",
            root = null
        )
        assertTrue(result is DetectionResult.Detected)
        val detected = result as DetectionResult.Detected
        assertEquals(ContentType.YOUTUBE_SHORTS, detected.contentType)
        assertTrue(detected.confidence >= 0.5f)
    }

    @Test
    fun `youtube detector confidence does not exceed 1f`() {
        val result = youtubeDetector.detect(
            packageName = YouTubeShortsDetector.YOUTUBE_PACKAGE,
            className = "com.google.android.apps.youtube.app.shorts.ShortsPivotFragment",
            root = null
        )
        if (result is DetectionResult.Detected) {
            assertTrue(result.confidence <= 1.0f)
        }
    }

    // --- Instagram Reels Detector ---

    @Test
    fun `instagram detector returns NotDetected for non-instagram package`() {
        val result = instagramDetector.detect(
            packageName = "com.example.other",
            className = "com.example.other.MainActivity",
            root = null
        )
        assertTrue(result is DetectionResult.NotDetected)
    }

    @Test
    fun `instagram detector returns NotDetected for instagram package with no reels signals`() {
        val result = instagramDetector.detect(
            packageName = InstagramReelsDetector.INSTAGRAM_PACKAGE,
            className = "com.instagram.android.activity.MainTabActivity",
            root = null
        )
        // No class or tree signals → not detected
        assertTrue(result is DetectionResult.NotDetected)
    }

    @Test
    fun `instagram detector returns Detected for known Reels class name`() {
        val result = instagramDetector.detect(
            packageName = InstagramReelsDetector.INSTAGRAM_PACKAGE,
            className = "com.instagram.android.ClipsViewerFragment",
            root = null
        )
        assertTrue(result is DetectionResult.Detected)
        val detected = result as DetectionResult.Detected
        assertEquals(ContentType.INSTAGRAM_REELS, detected.contentType)
        assertTrue(detected.confidence >= 0.5f)
    }

    @Test
    fun `instagram detector confidence does not exceed 1f`() {
        val result = instagramDetector.detect(
            packageName = InstagramReelsDetector.INSTAGRAM_PACKAGE,
            className = "com.instagram.android.ClipsViewerFragment",
            root = null
        )
        if (result is DetectionResult.Detected) {
            assertTrue(result.confidence <= 1.0f)
        }
    }

    // --- Cross-checks ---

    @Test
    fun `youtube detector is not triggered by instagram package`() {
        val result = youtubeDetector.detect(
            packageName = InstagramReelsDetector.INSTAGRAM_PACKAGE,
            className = "com.instagram.android.ClipsViewerFragment",
            root = null
        )
        assertTrue(result is DetectionResult.NotDetected)
    }

    @Test
    fun `instagram detector is not triggered by youtube package`() {
        val result = instagramDetector.detect(
            packageName = YouTubeShortsDetector.YOUTUBE_PACKAGE,
            className = "com.google.android.apps.youtube.app.shorts.ShortsPivotFragment",
            root = null
        )
        assertTrue(result is DetectionResult.NotDetected)
    }

    @Test
    fun `both detectors return NotDetected for empty package name`() {
        val ytResult = youtubeDetector.detect("", "", null)
        val igResult = instagramDetector.detect("", "", null)
        assertTrue(ytResult is DetectionResult.NotDetected)
        assertTrue(igResult is DetectionResult.NotDetected)
    }
}
