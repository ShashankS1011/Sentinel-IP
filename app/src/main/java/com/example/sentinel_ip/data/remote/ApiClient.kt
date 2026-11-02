package com.example.sentinel_ip.data.remote

import com.example.sentinel_ip.data.model.EndpointQuota
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
    private var currentApiKey: String = ""

    private val _rateLimitInfo = MutableStateFlow<RateLimitInfo?>(RateLimitInfo(1000, 1000))
    val rateLimitInfo: StateFlow<RateLimitInfo?> = _rateLimitInfo.asStateFlow()

    private val _endpointQuotas = MutableStateFlow<Map<String, EndpointQuota>>(EndpointQuota.getDefaultQuotas())
    val endpointQuotas: StateFlow<Map<String, EndpointQuota>> = _endpointQuotas.asStateFlow()

    fun updateApiKey(apiKey: String) {
        currentApiKey = apiKey.trim()
    }

    private val authAndRateLimitInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Accept", "application/json")

        if (currentApiKey.isNotBlank()) {
            requestBuilder.header("Key", currentApiKey)
        }

        val request = requestBuilder.build()
        val response = chain.proceed(request)

        // Parse AbuseIPDB Rate Limit Headers Per Service Endpoint
        try {
            val limitStr = response.header("X-RateLimit-Limit")
                ?: response.header("x-ratelimit-limit")
            val remainingStr = response.header("X-RateLimit-Remaining")
                ?: response.header("x-ratelimit-remaining")
            val resetStr = response.header("X-RateLimit-Reset")
                ?: response.header("x-ratelimit-reset")
            val retryAfterStr = response.header("Retry-After")
                ?: response.header("retry-after")

            val path = original.url.encodedPath
            val serviceKey = when {
                path.contains("check-block") -> "check-block"
                path.contains("check") -> "check"
                path.contains("blacklist") -> "blacklist"
                path.contains("reports") -> "reports"
                path.contains("report") -> "report"
                else -> "check"
            }

            val defaultQuota = EndpointQuota.getDefaultQuotas()[serviceKey] ?: EndpointQuota(serviceKey, "Service", 1000, 1000)

            if (limitStr != null || remainingStr != null) {
                val limit = limitStr?.toIntOrNull() ?: defaultQuota.limit
                val remaining = remainingStr?.toIntOrNull() ?: defaultQuota.remaining
                val reset = resetStr?.toLongOrNull() ?: 0L
                val retryAfter = retryAfterStr?.toLongOrNull() ?: 0L

                val updatedQuota = EndpointQuota(
                    serviceKey = serviceKey,
                    serviceName = defaultQuota.serviceName,
                    limit = limit,
                    remaining = remaining,
                    resetEpochSeconds = reset
                )

                val currentMap = _endpointQuotas.value.toMutableMap()
                currentMap[serviceKey] = updatedQuota
                _endpointQuotas.value = currentMap

                _rateLimitInfo.value = RateLimitInfo(
                    limit = limit,
                    remaining = remaining,
                    resetEpochSeconds = reset,
                    retryAfterSeconds = retryAfter
                )
            } else if (response.isSuccessful) {
                // If API headers are absent in response, decrement remaining counter by 1
                val currentQuota = _endpointQuotas.value[serviceKey] ?: defaultQuota
                val newRemaining = (currentQuota.remaining - 1).coerceAtLeast(0)
                val updatedQuota = currentQuota.copy(remaining = newRemaining)

                val currentMap = _endpointQuotas.value.toMutableMap()
                currentMap[serviceKey] = updatedQuota
                _endpointQuotas.value = currentMap

                _rateLimitInfo.value = RateLimitInfo(
                    limit = currentQuota.limit,
                    remaining = newRemaining,
                    resetEpochSeconds = currentQuota.resetEpochSeconds
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
