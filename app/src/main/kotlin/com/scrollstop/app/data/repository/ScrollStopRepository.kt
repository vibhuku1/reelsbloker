package com.scrollstop.app.data.repository

import android.content.Context
import androidx.room.Room
import com.scrollstop.app.data.database.BlockEventDao
import com.scrollstop.app.data.database.BlockEventEntity
import com.scrollstop.app.data.database.ScrollStopDatabase
import com.scrollstop.app.data.datastore.SettingsDataStore
import com.scrollstop.app.model.AppSettings
import com.scrollstop.app.model.BlockEvent
import com.scrollstop.app.model.ContentType
import com.scrollstop.app.model.Statistics
import com.scrollstop.app.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar

class ScrollStopRepository(context: Context) {

    private val database = Room.databaseBuilder(
        context.applicationContext,
        ScrollStopDatabase::class.java,
        "scrollstop_db"
    ).fallbackToDestructiveMigration().build()

    private val dao: BlockEventDao = database.blockEventDao()
    private val settingsDataStore = SettingsDataStore(context)

    // ---- Settings ----
    val settingsFlow: Flow<AppSettings> = settingsDataStore.settingsFlow

    suspend fun setEnabled(enabled: Boolean) = settingsDataStore.setEnabled(enabled)
    suspend fun setBlockShorts(block: Boolean) = settingsDataStore.setBlockShorts(block)
    suspend fun setBlockReels(block: Boolean) = settingsDataStore.setBlockReels(block)
    suspend fun setThemeMode(mode: ThemeMode) = settingsDataStore.setThemeMode(mode)
    suspend fun setOnboardingDone(done: Boolean) = settingsDataStore.setOnboardingDone(done)
    suspend fun setNotificationsEnabled(enabled: Boolean) = settingsDataStore.setNotificationsEnabled(enabled)

    // ---- Events ----
    suspend fun recordBlockEvent(event: BlockEvent) {
        dao.insert(
            BlockEventEntity(
                timestamp = event.timestamp,
                appPackage = event.appPackage,
                contentType = event.contentType.name,
                detectionSignals = event.detectionSignals
            )
        )
    }

    fun getAllEventsFlow(): Flow<List<BlockEvent>> =
        dao.getAllEvents().map { entities ->
            entities.map { entity ->
                BlockEvent(
                    id = entity.id,
                    timestamp = entity.timestamp,
                    appPackage = entity.appPackage,
                    contentType = ContentType.valueOf(entity.contentType),
                    detectionSignals = entity.detectionSignals
                )
            }
        }

    // ---- Statistics ----
    fun getStatisticsFlow(): Flow<Statistics> {
        val startOfDay = getStartOfDay()
        val startOfWeek = getStartOfWeek()
        val totalsAndToday = combine(
            dao.getTotalShortsBlocked(),
            dao.getTotalReelsBlocked(),
            dao.getTodayShortsBlocked(startOfDay),
            dao.getTodayReelsBlocked(startOfDay)
        ) { totalShorts, totalReels, todayShorts, todayReels ->
            listOf(totalShorts, totalReels, todayShorts, todayReels)
        }
        return combine(
            totalsAndToday,
            dao.getWeekShortsBlocked(startOfWeek),
            dao.getWeekReelsBlocked(startOfWeek)
        ) { counts, weekShorts, weekReels ->
            val totalShorts = counts[0]
            val totalReels = counts[1]
            val todayShorts = counts[2]
            val todayReels = counts[3]
            val totalBlocked = totalShorts + totalReels
            // Estimate: 3 minutes average per session avoided
            Statistics(
                totalShortsBlocked = totalShorts,
                totalReelsBlocked = totalReels,
                todayShortsBlocked = todayShorts,
                todayReelsBlocked = todayReels,
                weekShortsBlocked = weekShorts,
                weekReelsBlocked = weekReels,
                estimatedMinutesSaved = totalBlocked * 3
            )
        }
    }

    suspend fun clearAllData() {
        dao.deleteAll()
    }

    private fun getStartOfDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
