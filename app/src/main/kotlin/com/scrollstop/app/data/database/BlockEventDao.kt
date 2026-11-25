package com.scrollstop.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: BlockEventEntity)

    @Query("SELECT * FROM block_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<BlockEventEntity>>

    @Query("SELECT COUNT(*) FROM block_events WHERE contentType = 'YOUTUBE_SHORTS'")
    fun getTotalShortsBlocked(): Flow<Int>

    @Query("SELECT COUNT(*) FROM block_events WHERE contentType = 'INSTAGRAM_REELS'")
    fun getTotalReelsBlocked(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM block_events 
        WHERE contentType = 'YOUTUBE_SHORTS' 
        AND timestamp >= :startOfDay
    """)
    fun getTodayShortsBlocked(startOfDay: Long): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM block_events 
        WHERE contentType = 'INSTAGRAM_REELS' 
        AND timestamp >= :startOfDay
    """)
    fun getTodayReelsBlocked(startOfDay: Long): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM block_events 
        WHERE contentType = 'YOUTUBE_SHORTS' 
        AND timestamp >= :startOfWeek
    """)
    fun getWeekShortsBlocked(startOfWeek: Long): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM block_events 
        WHERE contentType = 'INSTAGRAM_REELS' 
        AND timestamp >= :startOfWeek
    """)
    fun getWeekReelsBlocked(startOfWeek: Long): Flow<Int>

    @Query("DELETE FROM block_events")
    suspend fun deleteAll()

    @Query("SELECT * FROM block_events WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getEventsAfter(startTime: Long): Flow<List<BlockEventEntity>>
}
