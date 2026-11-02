package com.example.sentinel_ip

import com.example.sentinel_ip.data.model.EndpointQuota
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EndpointQuotaTest {

    @Test
    fun testDefaultQuotasInitialization() {
        val quotas = EndpointQuota.getDefaultQuotas()
        assertNotNull(quotas)
        assertEquals(5, quotas.size)

        // Verify Check quota
        val checkQuota = quotas["check"]
        assertNotNull(checkQuota)
        assertEquals(1000, checkQuota?.limit)
        assertEquals(1000, checkQuota?.remaining)

        // Verify Blacklist quota
        val blacklistQuota = quotas["blacklist"]
        assertNotNull(blacklistQuota)
        assertEquals(5, blacklistQuota?.limit)
        assertEquals(5, blacklistQuota?.remaining)

        // Verify Check-Block quota
        val checkBlockQuota = quotas["check-block"]
        assertNotNull(checkBlockQuota)
        assertEquals(100, checkBlockQuota?.limit)
        assertEquals(100, checkBlockQuota?.remaining)
    }
}
