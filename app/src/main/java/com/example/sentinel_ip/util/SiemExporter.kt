package com.example.sentinel_ip.util

import com.example.sentinel_ip.data.model.CheckData
import com.google.gson.GsonBuilder

object SiemExporter {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun exportToJson(data: CheckData): String {
        return gson.toJson(data)
    }

    fun exportToCsv(data: CheckData): String {
        return "IPAddress,AbuseScore,Country,ISP,Domain,IsTor,TotalReports\n" +
                "${data.ipAddress},${data.abuseConfidenceScore},${data.countryCode ?: "N/A"},\"${data.isp ?: "N/A"}\",\"${data.domain ?: "N/A"}\",${data.isTor ?: false},${data.totalReports}"
    }

    fun exportToCef(data: CheckData): String {
        // Common Event Format (CEF) for SIEM (Splunk/ArcSight/QRadar)
        val severity = when {
            data.abuseConfidenceScore < 25 -> 2
            data.abuseConfidenceScore < 75 -> 6
            else -> 10
        }
        return "CEF:0|Sentinel-IP|AbuseIPDB|2.0|THREAT_AUDIT|Target IP Abuse Confidence Score|${severity}|src=${data.ipAddress} cs1=${data.countryCode ?: "N/A"} cs2=${data.isp ?: "N/A"} cnt=${data.totalReports}"
    }

    fun exportToStixJson(data: CheckData): String {
        // STIX 2.1 Standard Payload Format
        val stixMap = mapOf(
            "type" to "indicator",
            "spec_version" to "2.1",
            "id" to "indicator--${data.ipAddress.hashCode()}",
            "created" to System.currentTimeMillis(),
            "pattern" to "[ipv4-addr:value = '${data.ipAddress}']",
            "pattern_type" to "stix",
            "valid_from" to System.currentTimeMillis(),
            "confidence" to data.abuseConfidenceScore,
            "description" to "AbuseIPDB Threat Intelligence: ${data.isp ?: "Unknown ISP"} (${data.countryCode ?: "Global"})"
        )
        return gson.toJson(stixMap)
    }
}
