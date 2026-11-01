package com.example.sentinel_ip.data.remote

import com.example.sentinel_ip.BuildConfig
import com.example.sentinel_ip.data.model.RateLimitInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://api.abuseipdb.com/"

    @Volatile
    private var currentApiKey: String = BuildConfig.ABUSEIPDB_API_KEY

    private val _rateLimitInfo = MutableStateFlow<RateLimitInfo?>(null)
    val rateLimitInfo: StateFlow<RateLimitInfo?> = _rateLimitInfo.asStateFlow()

    fun updateApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        currentApiKey = if (trimmed.isNotBlank()) trimmed else BuildConfig.ABUSEIPDB_API_KEY
    }

    private val authAndRateLimitInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Accept", "application/json")

        val keyToUse = if (currentApiKey.isNotBlank()) currentApiKey else BuildConfig.ABUSEIPDB_API_KEY
        if (keyToUse.isNotBlank()) {
            requestBuilder.header("Key", keyToUse)
        }

        val request = requestBuilder.build()
        val response = chain.proceed(request)

        // Parse AbuseIPDB Rate Limit Headers
        try {
            val limitStr = response.header("X-RateLimit-Limit")
            val remainingStr = response.header("X-RateLimit-Remaining")
            val resetStr = response.header("X-RateLimit-Reset")
            val retryAfterStr = response.header("Retry-After")

            if (limitStr != null || remainingStr != null) {
                val limit = limitStr?.toIntOrNull() ?: _rateLimitInfo.value?.limit ?: 1000
                val remaining = remainingStr?.toIntOrNull() ?: _rateLimitInfo.value?.remaining ?: 1000
                val reset = resetStr?.toLongOrNull() ?: 0L
                val retryAfter = retryAfterStr?.toLongOrNull() ?: 0L

                _rateLimitInfo.value = RateLimitInfo(
                    limit = limit,
                    remaining = remaining,
                    resetEpochSeconds = reset,
                    retryAfterSeconds = retryAfter
                )
            }
        } catch (_: Exception) {
            // Ignore rate limit header parsing errors
        }

        response
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authAndRateLimitInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val apiService: AbuseIpApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AbuseIpApiService::class.java)
    }
}
