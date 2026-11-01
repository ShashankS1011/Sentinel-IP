package com.example.sentinel_ip.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.sentinel_ip.BuildConfig
import com.example.sentinel_ip.data.model.BookmarkedIp
import com.example.sentinel_ip.data.model.SearchHistoryItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sentinel_ip_prefs")

class PreferencesManager(private val context: Context) {

    private val gson = Gson()

    companion object {
        private val API_KEY = stringPreferencesKey("abuse_api_key")
        private val BOOKMARKS = stringPreferencesKey("bookmarked_ips")
        private val HISTORY = stringPreferencesKey("search_history")
    }

    val apiKeyFlow: Flow<String> = context.dataStore.data.map { prefs ->
        val savedKey = prefs[API_KEY]
        if (!savedKey.isNullOrBlank()) {
            savedKey
        } else {
            BuildConfig.ABUSEIPDB_API_KEY
        }
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[API_KEY] = key.trim()
        }
    }

    val bookmarkedIpsFlow: Flow<List<BookmarkedIp>> = context.dataStore.data.map { prefs ->
        val json = prefs[BOOKMARKS] ?: "[]"
        try {
            val type = object : TypeToken<List<BookmarkedIp>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun toggleBookmark(bookmark: BookmarkedIp) {
        context.dataStore.edit { prefs ->
            val json = prefs[BOOKMARKS] ?: "[]"
            val type = object : TypeToken<List<BookmarkedIp>>() {}.type
            val list: MutableList<BookmarkedIp> = try {
                gson.fromJson<List<BookmarkedIp>>(json, type)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            val existingIndex = list.indexOfFirst { it.ipAddress.equals(bookmark.ipAddress, ignoreCase = true) }
            if (existingIndex >= 0) {
                list.removeAt(existingIndex)
            } else {
                list.add(0, bookmark)
            }

            prefs[BOOKMARKS] = gson.toJson(list)
        }
    }

    val searchHistoryFlow: Flow<List<SearchHistoryItem>> = context.dataStore.data.map { prefs ->
        val json = prefs[HISTORY] ?: "[]"
        try {
            val type = object : TypeToken<List<SearchHistoryItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addSearchHistory(query: String) {
        if (query.isBlank()) return
        context.dataStore.edit { prefs ->
            val json = prefs[HISTORY] ?: "[]"
            val type = object : TypeToken<List<SearchHistoryItem>>() {}.type
            val list: MutableList<SearchHistoryItem> = try {
                gson.fromJson<List<SearchHistoryItem>>(json, type)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            list.removeAll { it.query.equals(query.trim(), ignoreCase = true) }
            list.add(0, SearchHistoryItem(query = query.trim()))

            // Keep max 30 items
            val trimmedList = if (list.size > 30) list.take(30) else list
            prefs[HISTORY] = gson.toJson(trimmedList)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { prefs ->
            prefs[HISTORY] = "[]"
        }
    }
}


