package com.example.sentinel_ip.data.repository

import com.example.sentinel_ip.data.local.PreferencesManager
import com.example.sentinel_ip.data.model.ApiErrorResponse
import com.example.sentinel_ip.data.model.BlacklistResponse
import com.example.sentinel_ip.data.model.BookmarkedIp
import com.example.sentinel_ip.data.model.CheckBlockResponse
import com.example.sentinel_ip.data.model.CheckData
import com.example.sentinel_ip.data.model.ClearAddressResponse
import com.example.sentinel_ip.data.model.ReportResponse
import com.example.sentinel_ip.data.model.ReportsResponse
import com.example.sentinel_ip.data.model.SearchHistoryItem
import com.example.sentinel_ip.data.remote.ApiClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val statusCode: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

class AbuseRepository(private val preferencesManager: PreferencesManager) {

    private val gson = Gson()

    val apiKeyFlow: Flow<String> = preferencesManager.apiKeyFlow
    val bookmarksFlow: Flow<List<BookmarkedIp>> = preferencesManager.bookmarkedIpsFlow
    val searchHistoryFlow: Flow<List<SearchHistoryItem>> = preferencesManager.searchHistoryFlow
    val rateLimitInfoFlow = ApiClient.rateLimitInfo

    suspend fun saveApiKey(key: String) {
        preferencesManager.saveApiKey(key)
        ApiClient.updateApiKey(key)
    }

    suspend fun checkIp(ipAddress: String, maxAgeInDays: Int = 30): ApiResult<CheckData> {
        return safeApiCall(
            call = { ApiClient.apiService.checkIp(ipAddress = ipAddress, maxAgeInDays = maxAgeInDays) },
            transform = { response -> response.data ?: throw Exception("No check data returned") }
        )
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

    suspend fun toggleBookmark(bookmark: BookmarkedIp) {
        preferencesManager.toggleBookmark(bookmark)
    }

    suspend fun addSearchHistory(query: String) {
        preferencesManager.addSearchHistory(query)
    }

    suspend fun clearHistory() {
        preferencesManager.clearHistory()
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
            val errorBodyStr = response.errorBody()?.string()
            if (!errorBodyStr.isNullOrBlank()) {
                val errorResponse = gson.fromJson(errorBodyStr, ApiErrorResponse::class.java)
                val detail = errorResponse?.errors?.firstOrNull()?.detail
                if (!detail.isNullOrBlank()) {
                    return detail
                }
            }
            when (response.code()) {
                401, 403 -> "Invalid or missing AbuseIPDB API Key. Please check your key in settings."
                429 -> "Daily API rate limit exceeded. Please try again later or upgrade your key."
                422 -> "Unprocessable Entity: Check query parameters."
                else -> "Server error ${response.code()}: ${response.message()}"
            }
        } catch (_: Exception) {
            "API error ${response.code()}: ${response.message()}"
        }
    }
}
