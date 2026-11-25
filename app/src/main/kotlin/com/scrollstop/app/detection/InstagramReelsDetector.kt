package com.scrollstop.app.detection

import android.view.accessibility.AccessibilityNodeInfo
import com.scrollstop.app.model.ContentType

/**
 * Detects Instagram Reels using multiple UI signals.
 *
 * Strategy (multi-signal approach):
 * 1. Package name confirms Instagram.
 * 2. Look for Reels-specific text labels.
 * 3. Look for Reels-specific resource-id fragments.
 * 4. Look for the bottom navigation tab selection pattern.
 * 5. Apply confidence threshold (≥ 0.5) before declaring detection.
 *
 * Safe fallback: Normal video posts, Stories, and DMs will not match the
 * Reels-specific signals and will return NotDetected.
 */
class InstagramReelsDetector {

    companion object {
        const val INSTAGRAM_PACKAGE = "com.instagram.android"

        // Text signals visible in the Reels UI
        private val REELS_TEXT_SIGNALS = setOf(
            "reels",
            "reel"
        )

        // Resource-id fragments that appear in Reels views
        private val REELS_RESOURCE_ID_FRAGMENTS = setOf(
            "clips_tab",
            "reels_tab",
            "clips_viewer",
            "clips_tray",
            "reels_tray",
            "clips_container",
            "reels_viewer_fragment",
            "clips_viewer_fragment",
            "reels_player"
        )

        // Class names for Reels fragment
        private val REELS_CLASS_SIGNALS = setOf(
            "ClipsViewerFragment",
            "ReelsViewerFragment",
            "ReelsTabFragment"
        )

        // Bottom nav Reels tab content description signals
        private val REELS_NAV_CONTENT_DESCRIPTIONS = setOf(
            "reels",
            "clips"
        )

        // Weights
        private const val WEIGHT_TEXT = 0.4f
        private const val WEIGHT_RESOURCE_ID = 0.5f
        private const val WEIGHT_CLASS = 0.6f
        private const val WEIGHT_NAV_SELECTED = 0.5f
        private const val THRESHOLD = 0.5f
    }

    fun detect(
        packageName: String,
        className: String,
        root: AccessibilityNodeInfo?
    ): DetectionResult {
        if (packageName != INSTAGRAM_PACKAGE) return DetectionResult.NotDetected

        val signals = mutableListOf<String>()
        var confidence = 0f

        // Signal 1: Class name
        if (REELS_CLASS_SIGNALS.any { className.contains(it, ignoreCase = true) }) {
            confidence += WEIGHT_CLASS
            signals.add("class:$className")
        }

        // Signal 2 & 3: Tree scan
        if (root != null) {
            val (textHit, resourceHit, navHit) = scanTree(root)
            if (textHit.isNotEmpty()) {
                confidence += WEIGHT_TEXT
                signals.addAll(textHit.map { "text:$it" })
            }
            if (resourceHit.isNotEmpty()) {
                confidence += WEIGHT_RESOURCE_ID
                signals.addAll(resourceHit.map { "id:$it" })
            }
            if (navHit) {
                confidence += WEIGHT_NAV_SELECTED
                signals.add("nav:reels_tab_selected")
            }
        }

        return if (confidence >= THRESHOLD) {
            DetectionResult.Detected(
                contentType = ContentType.INSTAGRAM_REELS,
                confidence = confidence.coerceAtMost(1f),
                signals = signals
            )
        } else {
            DetectionResult.NotDetected
        }
    }

    private fun scanTree(root: AccessibilityNodeInfo): Triple<List<String>, List<String>, Boolean> {
        val textHits = mutableListOf<String>()
        val idHits = mutableListOf<String>()
        var navTabSelected = false

        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var depth = 0

        while (queue.isNotEmpty() && depth < 8) {
            val levelSize = queue.size
            repeat(levelSize) {
                val node = queue.removeFirst()

                // Never inspect password fields or editable text inputs (e.g., DMs, search, comments)
                val nodeClass = node.className?.toString() ?: ""
                if (node.isPassword || nodeClass.contains("EditText", ignoreCase = true)) {
                    return@repeat
                }

                val text = node.text?.toString()?.lowercase() ?: ""
                val desc = node.contentDescription?.toString()?.lowercase() ?: ""
                val resId = node.viewIdResourceName?.lowercase() ?: ""

                // Text signals (but guard against false positives from comment text)
                // Only match if it's a tab, label or top-level descriptor, not deep content
                for (signal in REELS_TEXT_SIGNALS) {
                    if ((text == signal || desc == signal) && !textHits.contains(signal)) {
                        textHits.add(signal)
                    }
                }

                // Resource id signals
                for (fragment in REELS_RESOURCE_ID_FRAGMENTS) {
                    if (resId.contains(fragment) && !idHits.contains(fragment)) {
                        idHits.add(fragment)
                    }
                }

                // Nav tab selected check
                if (!navTabSelected && node.isSelected) {
                    val nodeDesc = desc.lowercase()
                    if (REELS_NAV_CONTENT_DESCRIPTIONS.any { nodeDesc.contains(it) }) {
                        navTabSelected = true
                    }
                }

                for (i in 0 until node.childCount) {
                    node.getChild(i)?.let { queue.add(it) }
                }
            }
            depth++
        }
        return Triple(textHits, idHits, navTabSelected)
    }
}
