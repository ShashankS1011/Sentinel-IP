package com.example.sentinel_ip

import com.example.sentinel_ip.data.model.AbuseCategory
import com.example.sentinel_ip.data.model.CheckResponse
import com.example.sentinel_ip.data.model.BlacklistResponse
import com.example.sentinel_ip.data.model.CheckBlockResponse
import com.example.sentinel_ip.data.model.ReportResponse
import com.example.sentinel_ip.ui.components.getThreatColor
import com.example.sentinel_ip.ui.components.getThreatLabel
import com.example.sentinel_ip.ui.theme.ThreatDanger
import com.example.sentinel_ip.ui.theme.ThreatSafe
import com.example.sentinel_ip.ui.theme.ThreatWarning
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AbuseModelsTest {

    private val gson = Gson()

    @Test
    fun testCheckResponseJsonDeserialization() {
        val json = """
            {
              "data": {
                "ipAddress": "118.25.6.39",
                "isPublic": true,
                "ipVersion": 4,
                "isWhitelisted": false,
                "abuseConfidenceScore": 100,
                "countryCode": "CN",
                "countryName": "China",
                "usageType": "Data Center/Web Hosting/Transit",
                "isp": "Tencent Cloud Computing",
                "domain": "tencent.com",
                "isTor": false,
                "totalReports": 15,
                "numDistinctUsers": 5,
                "reports": [
                  {
                    "reportedAt": "2026-03-20T20:55:14+00:00",
                    "comment": "Invalid user root from 118.25.6.39",
                    "categories": [18, 22]
                  }
                ]
              }
            }
        """.trimIndent()

        val response = gson.fromJson(json, CheckResponse::class.java)
        assertNotNull(response)
        assertNotNull(response.data)

        val data = response.data!!
        assertEquals("118.25.6.39", data.ipAddress)
        assertEquals(100, data.abuseConfidenceScore)
        assertEquals("CN", data.countryCode)
        assertEquals("Tencent Cloud Computing", data.isp)
        assertEquals(15, data.totalReports)
        assertFalse(data.isTor ?: true)
        assertEquals(1, data.reports?.size)
        assertEquals(2, data.reports!![0].categories.size)
    }

    @Test
    fun testBlacklistResponseJsonDeserialization() {
        val json = """
            {
              "meta": {
                "generatedAt": "2026-03-20T19:54:11+00:00"
              },
              "data": [
                {
                  "ipAddress": "5.188.10.179",
                  "countryCode": "RU",
                  "abuseConfidenceScore": 100,
                  "lastReportedAt": "2026-03-20T19:17:02+00:00"
                },
                {
                  "ipAddress": "185.222.209.14",
                  "countryCode": "US",
                  "abuseConfidenceScore": 85,
                  "lastReportedAt": "2026-03-20T19:17:02+00:00"
                }
              ]
            }
        """.trimIndent()

        val response = gson.fromJson(json, BlacklistResponse::class.java)
        assertNotNull(response)
        assertEquals(2, response.data.size)
        assertEquals("5.188.10.179", response.data[0].ipAddress)
        assertEquals(100, response.data[0].abuseConfidenceScore)
        assertEquals("185.222.209.14", response.data[1].ipAddress)
        assertEquals(85, response.data[1].abuseConfidenceScore)
    }

    @Test
    fun testCheckBlockResponseJsonDeserialization() {
        val json = """
            {
              "data": {
                "networkAddress": "127.0.0.0",
                "netmask": "255.255.255.0",
                "minAddress": "127.0.0.1",
                "maxAddress": "127.0.0.254",
                "numPossibleHosts": 254,
                "addressSpaceDesc": "Loopback",
                "reportedAddress": [
                  {
                    "ipAddress": "127.0.0.1",
                    "numReports": 631,
                    "abuseConfidenceScore": 50
                  }
                ]
              }
            }
        """.trimIndent()

        val response = gson.fromJson(json, CheckBlockResponse::class.java)
        assertNotNull(response)
        assertNotNull(response.data)
        assertEquals("127.0.0.0", response.data!!.networkAddress)
        assertEquals(254L, response.data!!.numPossibleHosts)
        assertEquals(1, response.data!!.reportedAddress.size)
        assertEquals("127.0.0.1", response.data!!.reportedAddress[0].ipAddress)
    }

    @Test
    fun testReportResponseJsonDeserialization() {
        val json = """
            {
              "data": {
                "ipAddress": "127.0.0.1",
                "abuseConfidenceScore": 52
              }
            }
        """.trimIndent()

        val response = gson.fromJson(json, ReportResponse::class.java)
        assertNotNull(response)
        assertNotNull(response.data)
        assertEquals("127.0.0.1", response.data!!.ipAddress)
        assertEquals(52, response.data!!.abuseConfidenceScore)
    }

    @Test
    fun testAbuseCategories() {
        assertEquals("SSH Brute-Force", AbuseCategory.getCategoryName(22))
        assertEquals("Port Scan", AbuseCategory.getCategoryName(14))
        assertEquals("DDoS Attack", AbuseCategory.getCategoryName(4))
        assertEquals("Category 999", AbuseCategory.getCategoryName(999)) // Unknown fallback
    }

    @Test
    fun testThreatScoreLevelsAndColors() {
        // Safe (0 - 24)
        assertEquals(ThreatSafe, getThreatColor(0))
        assertEquals(ThreatSafe, getThreatColor(20))
        assertEquals("Clean / Safe", getThreatLabel(0))
        assertEquals("Low Risk", getThreatLabel(15))

        // Warning / Suspicious (25 - 74)
        assertEquals(ThreatWarning, getThreatColor(25))
        assertEquals(ThreatWarning, getThreatColor(50))
        assertEquals("Suspicious", getThreatLabel(50))

        // Critical Threat (75 - 100)
        assertEquals(ThreatDanger, getThreatColor(75))
        assertEquals(ThreatDanger, getThreatColor(100))
        assertEquals("Malicious / Dangerous", getThreatLabel(90))
    }
}
