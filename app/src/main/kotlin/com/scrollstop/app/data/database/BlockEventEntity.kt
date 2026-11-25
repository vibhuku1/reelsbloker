package com.scrollstop.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_events")
data class BlockEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val appPackage: String,
    val contentType: String,  // "YOUTUBE_SHORTS" or "INSTAGRAM_REELS"
    val detectionSignals: String = ""
)
