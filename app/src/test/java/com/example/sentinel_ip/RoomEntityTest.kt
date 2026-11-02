package com.example.sentinel_ip

import com.example.sentinel_ip.data.local.room.BookmarkedIpEntity
import com.example.sentinel_ip.data.local.room.SearchHistoryEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomEntityTest {

    @Test
    fun testBookmarkedIpEntityCreation() {
        val entity = BookmarkedIpEntity(
            "185.220.101.5",
            95,
            "DE",
            "Tor Exit Node ISP",
            "tor-exit.com",
            1700000000000L
        )
        assertEquals("185.220.101.5", entity.ipAddress)
        assertEquals(95, entity.abuseConfidenceScore)
        assertEquals("DE", entity.countryCode)
        assertEquals("Tor Exit Node ISP", entity.isp)
    }

    @Test
    fun testSearchHistoryEntityCreation() {
        val history = SearchHistoryEntity(
            1,
            "badhost.ru",
            "185.220.101.5",
            1700000000000L
        )
        assertEquals(1, history.id)
        assertEquals("badhost.ru", history.query)
        assertEquals("185.220.101.5", history.resolvedIp)
    }
}
