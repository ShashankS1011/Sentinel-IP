package com.example.sentinel_ip.data.repository

import android.content.Context
import com.example.sentinel_ip.data.local.PreferencesManager
import com.example.sentinel_ip.data.local.room.BookmarkedIpEntity
import com.example.sentinel_ip.data.local.room.OfflineCacheEntity
import com.example.sentinel_ip.data.local.room.SearchHistoryEntity
import com.example.sentinel_ip.data.local.room.SentinelDatabase
import com.example.sentinel_ip.data.model.ApiErrorResponse
import com.example.sentinel_ip.data.model.BlacklistResponse
import com.example.sentinel_ip.data.model.BookmarkedIp
import com.example.sentinel_ip.data.model.CheckBlockResponse
import com.example.sentinel_ip.data.model.CheckData
import com.example.sentinel_ip.data.model.ClearAddressResponse
import com.example.sentinel_ip.data.model.ReportResponse
import com.example.sentinel_ip.data.model.ReportsResponse
import com.example.sentinel_ip.data.remote.ApiClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.InetAddress

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val statusCode: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

data class ResolvedTarget(
    val query: String,
    val resolvedIp: String,
    val isDomainResolved: Boolean
)

data class BulkAuditResult(
    val totalExtractedIps: Int,
    val auditResults: List<CheckData>
)

class AbuseRepository(
    context: Context,
    private val preferencesManager: PreferencesManager
) {

    private val gson = Gson()
    private val roomDao = SentinelDatabase.getDatabase(context).sentinelDao()

    val apiKeyFlow: Flow<String> = preferencesManager.apiKeyFlow
    val rateLimitInfoFlow = ApiClient.rateLimitInfo
    val endpointQuotasFlow = ApiClient.endpointQuotas

    private val _roomBookmarksState = MutableStateFlow<List<BookmarkedIp>>(emptyList())
    val roomBookmarksFlow: StateFlow<List<BookmarkedIp>> = _roomBookmarksState.asStateFlow()

    private val _roomSearchHistoryState = MutableStateFlow<List<SearchHistoryEntity>>(emptyList())
    val roomSearchHistoryFlow: StateFlow<List<SearchHistoryEntity>> = _roomSearchHistoryState.asStateFlow()

    suspend fun refreshBookmarks() = withContext(Dispatchers.IO) {
        val list = roomDao.allBookmarks.map { entity ->
            BookmarkedIp(
                ipAddress = entity.ipAddress,
                abuseConfidenceScore = entity.abuseConfidenceScore,
                countryCode = entity.countryCode,
                isp = entity.isp
            )
        }
        _roomBookmarksState.value = list
    }

    suspend fun refreshHistory() = withContext(Dispatchers.IO) {
        _roomSearchHistoryState.value = roomDao.searchHistory
    }

    suspend fun saveApiKey(key: String) {
        preferencesManager.saveApiKey(key)
        ApiClient.updateApiKey(key)
    }

    // --- RESOLVE DOMAIN TO IP ---
    suspend fun resolveTarget(input: String): ResolvedTarget = withContext(Dispatchers.IO) {
        val trimmed = input.trim()
        val ipRegex = Regex("""^(?:[0-9]{1,3}\.){3}[0-9]{1,3}|(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}${'$'}""")

        if (ipRegex.matches(trimmed)) {
            ResolvedTarget(query = trimmed, resolvedIp = trimmed, isDomainResolved = false)
        } else {
            try {
                val address = InetAddress.getByName(trimmed)
                val resolvedIp = address.hostAddress ?: trimmed
                ResolvedTarget(query = trimmed, resolvedIp = resolvedIp, isDomainResolved = true)
            } catch (_: Exception) {
                ResolvedTarget(query = trimmed, resolvedIp = trimmed, isDomainResolved = false)
            }
        }
    }

    suspend fun checkIp(ipAddress: String, maxAgeInDays: Int = 30): ApiResult<CheckData> {
        val result = safeApiCall(
            call = { ApiClient.apiService.checkIp(ipAddress = ipAddress, maxAgeInDays = maxAgeInDays) },
            transform = { response -> response.data ?: throw Exception("No check data returned") }
        )

        // Cache result in Room for offline resilience
        if (result is ApiResult.Success) {
            withContext(Dispatchers.IO) {
                val json = gson.toJson(result.data)
                roomDao.cacheResponse(OfflineCacheEntity(ipAddress, json, System.currentTimeMillis()))
            }
        }

        return result
    }

    // --- BULK IP PARSER & CONCURRENT AUDIT ---
    suspend fun auditBulkIps(rawLogText: String): ApiResult<BulkAuditResult> = coroutineScope {
        val ipRegex = Regex("""\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b""")
        val extractedIps = ipRegex.findAll(rawLogText).map { it.value }.distinct().toList()

        if (extractedIps.isEmpty()) {
            return@coroutineScope ApiResult.Error("No valid IPv4 addresses found in the provided log text.")
        }

        val deferredAudits = extractedIps.take(10).map { ip ->
            async {
                when (val res = checkIp(ip)) {
                    is ApiResult.Success -> res.data
                    else -> null
                }
            }
        }

        val results = deferredAudits.awaitAll().filterNotNull().sortedByDescending { it.abuseConfidenceScore }
        ApiResult.Success(BulkAuditResult(totalExtractedIps = extractedIps.size, auditResults = results))
    }

    suspend fun getReports(ipAddress: String, page: Int = 1, perPage: Int = 25): ApiResult<ReportsResponse> {
        return safeApiCall(
            call = { ApiClient.apiService.getReports(ipAddress = ipAddress, page = page, perPage = perPage) },
            transform = { it }
        )
    }

    suspend fun getBlacklist(
        confidenceMinimum: Int = 90,
        limit: Int = 10000,
        onlyCountries: String? = null,
        exceptCountries: String? = null,
        ipVersion: Int? = null
    ): ApiResult<BlacklistResponse> {
        return safeApiCall(
            call = {
                ApiClient.apiService.getBlacklist(
                    confidenceMinimum = confidenceMinimum,
                    limit = limit,
                    onlyCountries = onlyCountries.takeIf { !it.isNullOrBlank() },
                    exceptCountries = exceptCountries.takeIf { !it.isNullOrBlank() },
                    ipVersion = ipVersion
                )
            },
            transform = { it }
        )
    }

    suspend fun reportIp(
        ip: String,
        categories: List<Int>,
        comment: String,
        timestamp: String? = null
    ): ApiResult<ReportResponse> {
        if (categories.isEmpty()) {
            return ApiResult.Error("Select at least one threat category.")
        }
        val categoriesStr = categories.joinToString(",")
        return safeApiCall(
            call = {
                ApiClient.apiService.reportIp(
                    ip = ip,
                    categories = categoriesStr,
                    comment = comment,
                    timestamp = timestamp.takeIf { !it.isNullOrBlank() }
                )
            },
            transform = { it }
        )
    }

    suspend fun checkBlock(network: String, maxAgeInDays: Int = 30): ApiResult<CheckBlockResponse> {
        return safeApiCall(
            call = { ApiClient.apiService.checkBlock(network = network, maxAgeInDays = maxAgeInDays) },
            transform = { it }
        )
    }

    suspend fun clearAddress(ipAddress: String): ApiResult<ClearAddressResponse> {
        return safeApiCall(
            call = { ApiClient.apiService.clearAddress(ipAddress = ipAddress) },
            transform = { it }
        )
    }

    // --- ROOM BOOKMARK & HISTORY ACTIONS ---
    suspend fun toggleBookmark(bookmark: BookmarkedIpEntity) = withContext(Dispatchers.IO) {
        if (roomDao.isBookmarked(bookmark.ipAddress)) {
            roomDao.deleteBookmark(bookmark.ipAddress)
        } else {
            roomDao.insertBookmark(bookmark)
        }
        refreshBookmarks()
    }

    suspend fun addSearchHistory(query: String, resolvedIp: String?) = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext
        roomDao.insertHistory(SearchHistoryEntity(0, query.trim(), resolvedIp, System.currentTimeMillis()))
        refreshHistory()
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        roomDao.clearHistory()
        refreshHistory()
    }

    private suspend fun <T, R> safeApiCall(
        call: suspend () -> Response<T>,
        transform: (T) -> R
    ): ApiResult<R> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(transform(response.body()!!))
            } else {
                val errorMsg = parseErrorMessage(response)
                ApiResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network request failed. Check your internet connection.")
        }
    }

    private fun <T> parseErrorMessage(response: Response<T>): String {
        return try {
            if (response.code() == 401 || response.code() == 403) {
                return "API Key missing or invalid. Please tap Settings (top right) to enter your AbuseIPDB API key."
            }
            val errorBodyStr = response.errorBody()?.string()
            if (!errorBodyStr.isNullOrBlank()) {
                val errorResponse = gson.fromJson(errorBodyStr, ApiErrorResponse::class.java)
                val detail = errorResponse?.errors?.firstOrNull()?.detail
                if (!detail.isNullOrBlank()) {
                    if (detail.contains("Authentication failed", ignoreCase = true) || detail.contains("API key", ignoreCase = true)) {
                        return "API Key missing or invalid. Please tap Settings (top right) to enter your AbuseIPDB API key."
                    }
                    return detail
                }
            }
            when (response.code()) {
                429 -> "Daily API rate limit exceeded. Please try again later or upgrade your key."
                422 -> "Unprocessable Entity: Check query parameters."
                else -> "Server error ${response.code()}: ${response.message()}"
            }
        } catch (_: Exception) {
            "API error ${response.code()}: ${response.message()}"
        }
    }
}
