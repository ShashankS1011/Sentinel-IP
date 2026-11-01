package com.example.sentinel_ip.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SentinelDao {

    // --- BOOKMARKS ---
    @Query("SELECT * FROM bookmarked_ips ORDER BY savedTimestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkedIpEntity>>

    @Query("SELECT * FROM bookmarked_ips ORDER BY abuseConfidenceScore DESC")
    fun getBookmarksByScoreDesc(): Flow<List<BookmarkedIpEntity>>

    @Query("SELECT * FROM bookmarked_ips ORDER BY countryCode ASC")
    fun getBookmarksByCountry(): Flow<List<BookmarkedIpEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkedIpEntity)

    @Query("DELETE FROM bookmarked_ips WHERE LOWER(ipAddress) = LOWER(:ipAddress)")
    suspend fun deleteBookmark(ipAddress: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_ips WHERE LOWER(ipAddress) = LOWER(:ipAddress))")
    suspend fun isBookmarked(ipAddress: String): Boolean

    // --- SEARCH HISTORY ---
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 30")
    fun getSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(historyItem: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()

    // --- OFFLINE AUDIT CACHE ---
    @Query("SELECT jsonResponse FROM offline_cache WHERE LOWER(ipAddress) = LOWER(:ipAddress)")
    suspend fun getCachedResponse(ipAddress: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheResponse(cache: OfflineCacheEntity)
}
