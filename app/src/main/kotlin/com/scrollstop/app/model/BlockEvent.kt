package com.scrollstop.app.model

data class BlockEvent(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val appPackage: String,
    val contentType: ContentType,
    val detectionSignals: String = ""
)

enum class ContentType(val displayName: String) {
    YOUTUBE_SHORTS("YouTube Shorts"),
    INSTAGRAM_REELS("Instagram Reels")
}
