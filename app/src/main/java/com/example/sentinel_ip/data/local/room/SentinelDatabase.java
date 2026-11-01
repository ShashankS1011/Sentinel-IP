package com.example.sentinel_ip.data.local.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
    entities = {
        BookmarkedIpEntity.class,
        SearchHistoryEntity.class,
        OfflineCacheEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class SentinelDatabase extends RoomDatabase {

    public abstract SentinelDao sentinelDao();

    private static volatile SentinelDatabase INSTANCE;

    public static SentinelDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SentinelDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        SentinelDatabase.class,
                        "sentinel_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
