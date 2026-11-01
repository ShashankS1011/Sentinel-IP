package com.example.sentinel_ip.data.local.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "offline_cache")
public class OfflineCacheEntity {

    @PrimaryKey
    @NonNull
    public String ipAddress;
    public String jsonResponse;
    public long cachedTimestamp;

    public OfflineCacheEntity(@NonNull String ipAddress, String jsonResponse, long cachedTimestamp) {
        this.ipAddress = ipAddress;
        this.jsonResponse = jsonResponse;
        this.cachedTimestamp = cachedTimestamp == 0 ? System.currentTimeMillis() : cachedTimestamp;
    }
}
