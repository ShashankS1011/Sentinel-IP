package com.example.sentinel_ip.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookmarkedIpEntity::class,
        SearchHistoryEntity::class,
        OfflineCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SentinelDatabase : RoomDatabase() {

    abstract fun sentinelDao(): SentinelDao

    companion object {
        @Volatile
        private var INSTANCE: SentinelDatabase? = null

        fun getDatabase(context: Context): SentinelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SentinelDatabase::class.java,
                    "sentinel_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
