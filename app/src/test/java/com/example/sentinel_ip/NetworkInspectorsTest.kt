package com.example.sentinel_ip

import com.example.sentinel_ip.data.remote.PortScanResult
import com.example.sentinel_ip.data.remote.SslCertResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkInspectorsTest {

    @Test
    fun testPortScanResultDataClass() {
        val result = PortScanResult(
            port = 22,
            serviceName = "SSH",
            isOpen = true
        )
        assertEquals(22, result.port)
        assertEquals("SSH", result.serviceName)
        assertTrue(result.isOpen)
    }

    @Test
    fun testSslCertResultDataClass() {
        val cert = SslCertResult(
            issuer = "Let's Encrypt",
            subject = "example.com",
            validFrom = "2026-01-01",
            validTo = "2026-12-31",
            daysUntilExpiry = 120
        )
        assertEquals("Let's Encrypt", cert.issuer)
        assertEquals("example.com", cert.subject)
        assertEquals(120L, cert.daysUntilExpiry)
    }
}
