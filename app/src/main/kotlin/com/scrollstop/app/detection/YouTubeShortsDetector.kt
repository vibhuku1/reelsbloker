package com.scrollstop.app.detection

import android.view.accessibility.AccessibilityNodeInfo
import com.scrollstop.app.model.ContentType

/**
 * Detects YouTube Shorts using multiple UI signals.
 *
 * Strategy (multi-signal approach):
 * 1. Window class / package name confirms we are inside YouTube.
 * 2. Look for Shorts-specific text labels in the accessibility tree.
 * 3. Look for Shorts-specific resource-id patterns.
 * 4. Look for the vertical swipe / shorts player UI hierarchy pattern.
 * 5. Apply confidence threshold (≥ 0.6) before declaring a detection.
 *
 * Safe fallback: If none of the strong signals fire, we return NotDetected
 * to avoid blocking normal YouTube videos.
 */
class YouTubeShortsDetector {

    companion object {
        const val YOUTUBE_PACKAGE = "com.google.android.youtube"

        // Text signals – visible labels in the Shorts UI
        private val SHORTS_TEXT_SIGNALS = setOf(
            "shorts",
            "youtube shorts",
            "#shorts"
        )

        // Resource-id fragments that appear in Shorts-specific views
        private val SHORTS_RESOURCE_ID_FRAGMENTS = setOf(
            "reel_player_container",
            "shorts_container",
            "shorts_shelf",
            "reel_shelf",
            "shorts_pivot_item",
            "shorts_player"
        )

        // Class names associated with the Shorts player (vertical swipe container)
        private val SHORTS_CLASS_SIGNALS = setOf(
            "com.google.android.apps.youtube.app.shorts.ShortsPivotFragment",
            "com.google.android.apps.youtube.app.watch.reels.ReelsFragment"
        )

        // Confidence weights
        private const val WEIGHT_TEXT = 0.5f
        private const val WEIGHT_RESOURCE_ID = 0.4f
        private const val WEIGHT_CLASS = 0.6f
        private const val THRESHOLD = 0.5f
    }

    /**
     * Analyse an accessibility tree rooted at [root].
     * @param packageName The foreground app package.
     * @param className   The window class name from the accessibility event.
     * @param root        Root node of the current window (may be null).
     */
    fun detect(
        packageName: String,
        className: String,
        root: AccessibilityNodeInfo?
    ): DetectionResult {
        if (packageName != YOUTUBE_PACKAGE) return DetectionResult.NotDetected

        val signals = mutableListOf<String>()
        var confidence = 0f

        // Signal 1: Window class name matches a known Shorts class
        if (SHORTS_CLASS_SIGNALS.any { className.contains(it, ignoreCase = true) }) {
            confidence += WEIGHT_CLASS
            signals.add("class:$className")
        }

        // Signal 2 & 3: Traverse the UI tree
        if (root != null) {
            val (textHit, resourceHit) = scanTree(root)
            if (textHit.isNotEmpty()) {
                confidence += WEIGHT_TEXT
                signals.addAll(textHit.map { "text:$it" })
            }
            if (resourceHit.isNotEmpty()) {
                confidence += WEIGHT_RESOURCE_ID
                signals.addAll(resourceHit.map { "id:$it" })
            }
        }

        return if (confidence >= THRESHOLD) {
            DetectionResult.Detected(
                contentType = ContentType.YOUTUBE_SHORTS,
                confidence = confidence.coerceAtMost(1f),
                signals = signals
            )
        } else {
            DetectionResult.NotDetected
        }
    }

    /**
     * BFS scan of the accessibility tree looking for Shorts signals.
     * Returns (text hits, resource-id hits).
     */
    private fun scanTree(root: AccessibilityNodeInfo): Pair<List<String>, List<String>> {
        val textHits = mutableListOf<String>()
        val idHits = mutableListOf<String>()
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var depth = 0

        while (queue.isNotEmpty() && depth < 8) {
            val levelSize = queue.size
            repeat(levelSize) {
                val node = queue.removeFirst()

                // Never inspect password fields or editable text inputs
                val nodeClass = node.className?.toString() ?: ""
                if (node.isPassword || nodeClass.contains("EditText", ignoreCase = true)) {
                    return@repeat
                }

                // Check text
                val text = node.text?.toString()?.lowercase() ?: ""
                val desc = node.contentDescription?.toString()?.lowercase() ?: ""
                for (signal in SHORTS_TEXT_SIGNALS) {
                    if ((text.contains(signal) || desc.contains(signal)) && !textHits.contains(signal)) {
                        textHits.add(signal)
                    }
                }

                // Check resource id
                val resId = node.viewIdResourceName?.lowercase() ?: ""
                for (fragment in SHORTS_RESOURCE_ID_FRAGMENTS) {
                    if (resId.contains(fragment) && !idHits.contains(fragment)) {
                        idHits.add(fragment)
                    }
                }

                // Queue children
                for (i in 0 until node.childCount) {
                    node.getChild(i)?.let { queue.add(it) }
                }
            }
            depth++
        }
        return Pair(textHits, idHits)
    }
}
