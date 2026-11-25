package com.scrollstop.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BlockEventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ScrollStopDatabase : RoomDatabase() {
    abstract fun blockEventDao(): BlockEventDao
}
