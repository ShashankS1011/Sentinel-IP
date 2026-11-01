package com.example.sentinel_ip.data.remote

import com.example.sentinel_ip.data.model.BlacklistResponse
import com.example.sentinel_ip.data.model.CheckBlockResponse
import com.example.sentinel_ip.data.model.CheckResponse
import com.example.sentinel_ip.data.model.ClearAddressResponse
import com.example.sentinel_ip.data.model.ReportResponse
import com.example.sentinel_ip.data.model.ReportsResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AbuseIpApiService {

    @GET("api/v2/check")
    suspend fun checkIp(
        @Query("ipAddress") ipAddress: String,
        @Query("maxAgeInDays") maxAgeInDays: Int = 30,
        @Query("verbose") verbose: String? = "verbose"
    ): Response<CheckResponse>

    @GET("api/v2/reports")
    suspend fun getReports(
        @Query("ipAddress") ipAddress: String,
        @Query("maxAgeInDays") maxAgeInDays: Int = 30,
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 25
    ): Response<ReportsResponse>

    @GET("api/v2/blacklist")
    suspend fun getBlacklist(
        @Query("confidenceMinimum") confidenceMinimum: Int = 90,
        @Query("limit") limit: Int = 10000,
        @Query("onlyCountries") onlyCountries: String? = null,
        @Query("exceptCountries") exceptCountries: String? = null,
        @Query("ipVersion") ipVersion: Int? = null
    ): Response<BlacklistResponse>

    @FormUrlEncoded
    @POST("api/v2/report")
    suspend fun reportIp(
        @Field("ip") ip: String,
        @Field("categories") categories: String,
        @Field("comment") comment: String,
        @Field("timestamp") timestamp: String? = null
    ): Response<ReportResponse>

    @GET("api/v2/check-block")
    suspend fun checkBlock(
        @Query("network") network: String,
        @Query("maxAgeInDays") maxAgeInDays: Int = 30
    ): Response<CheckBlockResponse>

    @DELETE("api/v2/clear-address")
    suspend fun clearAddress(
        @Query("ipAddress") ipAddress: String
    ): Response<ClearAddressResponse>
}
