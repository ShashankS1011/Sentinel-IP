package com.example.sentinel_ip.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarked_ips")
data class BookmarkedIpEntity(
    @PrimaryKey val ipAddress: String,
    val abuseConfidenceScore: Int,
    val countryCode: String?,
    val isp: String?,
    val domain: String?,
    val savedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val resolvedIp: String?,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "offline_cache")
data class OfflineCacheEntity(
    @PrimaryKey val ipAddress: String,
    val jsonResponse: String,
    val cachedTimestamp: Long = System.currentTimeMillis()
)
