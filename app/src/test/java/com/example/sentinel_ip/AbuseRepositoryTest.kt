package com.example.sentinel_ip

import com.example.sentinel_ip.data.model.BookmarkedIp
import com.example.sentinel_ip.data.model.SearchHistoryItem
import com.example.sentinel_ip.data.repository.ApiResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AbuseRepositoryTest {

    @Test
    fun testApiResultClasses() {
        val successResult = ApiResult.Success("118.25.6.39")
        assertTrue(successResult is ApiResult.Success)
        assertEquals("118.25.6.39", successResult.data)

        val errorResult = ApiResult.Error("API rate limit exceeded", 429)
        assertTrue(errorResult is ApiResult.Error)
        assertEquals("API rate limit exceeded", errorResult.message)
        assertEquals(429, errorResult.statusCode)

        val loadingResult = ApiResult.Loading
        assertTrue(loadingResult is ApiResult.Loading)
    }

    @Test
    fun testBookmarkedIpCreation() {
        val bookmark = BookmarkedIp(
            ipAddress = "176.111.173.242",
            abuseConfidenceScore = 85,
            countryCode = "DE",
            isp = "Example Hosting Provider"
        )

        assertEquals("176.111.173.242", bookmark.ipAddress)
        assertEquals(85, bookmark.abuseConfidenceScore)
        assertEquals("DE", bookmark.countryCode)
        assertEquals("Example Hosting Provider", bookmark.isp)
    }

    @Test
    fun testSearchHistoryItem() {
        val item = SearchHistoryItem(query = "192.168.1.0/24")
        assertEquals("192.168.1.0/24", item.query)
        assertTrue(item.timestamp > 0)
    }
}
