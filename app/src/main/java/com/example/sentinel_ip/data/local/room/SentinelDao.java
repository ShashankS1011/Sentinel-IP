package com.example.sentinel_ip.data.local.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SentinelDao {

    // --- BOOKMARKS ---
    @Query("SELECT * FROM bookmarked_ips ORDER BY savedTimestamp DESC")
    List<BookmarkedIpEntity> getAllBookmarks();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBookmark(BookmarkedIpEntity bookmark);

    @Query("DELETE FROM bookmarked_ips WHERE LOWER(ipAddress) = LOWER(:ipAddress)")
    void deleteBookmark(String ipAddress);

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_ips WHERE LOWER(ipAddress) = LOWER(:ipAddress))")
    boolean isBookmarked(String ipAddress);

    // --- SEARCH HISTORY ---
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 30")
    List<SearchHistoryEntity> getSearchHistory();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(SearchHistoryEntity historyItem);

    @Query("DELETE FROM search_history")
    void clearHistory();

    // --- OFFLINE AUDIT CACHE ---
    @Query("SELECT jsonResponse FROM offline_cache WHERE LOWER(ipAddress) = LOWER(:ipAddress)")
    String getCachedResponse(String ipAddress);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void cacheResponse(OfflineCacheEntity cache);
}
