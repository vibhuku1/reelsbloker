package com.scrollstop.app.model

data class Statistics(
    val totalShortsBlocked: Int = 0,
    val totalReelsBlocked: Int = 0,
    val todayShortsBlocked: Int = 0,
    val todayReelsBlocked: Int = 0,
    val weekShortsBlocked: Int = 0,
    val weekReelsBlocked: Int = 0,
    val estimatedMinutesSaved: Int = 0
) {
    val totalBlocked: Int get() = totalShortsBlocked + totalReelsBlocked
    val todayBlocked: Int get() = todayShortsBlocked + todayReelsBlocked
}
