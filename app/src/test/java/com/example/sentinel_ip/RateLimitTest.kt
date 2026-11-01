package com.example.sentinel_ip

import com.example.sentinel_ip.data.model.RateLimitInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RateLimitTest {

    @Test
    fun testRateLimitInfoState() {
        val rateLimit = RateLimitInfo(
            limit = 1000,
            remaining = 250,
            resetEpochSeconds = 1793527200L,
            retryAfterSeconds = 0
        )

        assertEquals(1000, rateLimit.limit)
        assertEquals(250, rateLimit.remaining)
        assertEquals(1793527200L, rateLimit.resetEpochSeconds)
        assertFalse(rateLimit.remaining <= 0)
    }

    @Test
    fun testExceededRateLimitState() {
        val exceededRateLimit = RateLimitInfo(
            limit = 1000,
            remaining = 0,
            resetEpochSeconds = 1793527200L,
            retryAfterSeconds = 3600
        )

        assertEquals(0, exceededRateLimit.remaining)
        assertTrue(exceededRateLimit.remaining <= 0)
        assertEquals(3600L, exceededRateLimit.retryAfterSeconds)
    }
}
