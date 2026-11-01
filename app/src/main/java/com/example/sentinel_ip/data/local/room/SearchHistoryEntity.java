package com.example.sentinel_ip.data.local.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "search_history")
public class SearchHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String query;
    public String resolvedIp;
    public long timestamp;

    public SearchHistoryEntity(long id, String query, String resolvedIp, long timestamp) {
        this.id = id;
        this.query = query;
        this.resolvedIp = resolvedIp;
        this.timestamp = timestamp == 0 ? System.currentTimeMillis() : timestamp;
    }
}
