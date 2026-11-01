package com.example.sentinel_ip.data.model

data class EndpointQuota(
    val serviceKey: String,        // "check", "blacklist", "check-block", "report", "reports"
    val serviceName: String,       // "IP Audit", "Global Radar", "Subnet Scanner", "Abuse Reporter", "Report History"
    val limit: Int,
    val remaining: Int,
    val resetEpochSeconds: Long = 0
) {
    companion object {
        fun getDefaultQuotas(): Map<String, EndpointQuota> {
            return mapOf(
                "check" to EndpointQuota("check", "IP Audit Service", 1000, 1000),
                "blacklist" to EndpointQuota("blacklist", "Global Radar Service", 5, 5),
                "check-block" to EndpointQuota("check-block", "Subnet Scanner Service", 100, 100),
                "report" to EndpointQuota("report", "Abuse Reporter Service", 1000, 1000),
                "reports" to EndpointQuota("reports", "Reports History Service", 100, 100)
            )
        }
    }
}
