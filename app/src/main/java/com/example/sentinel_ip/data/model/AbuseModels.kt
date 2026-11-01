package com.example.sentinel_ip.data.model

import com.google.gson.annotations.SerializedName

// --- CHECK ENDPOINT MODELS ---
data class CheckResponse(
    @SerializedName("data") val data: CheckData?
)

data class CheckData(
    @SerializedName("ipAddress") val ipAddress: String,
    @SerializedName("isPublic") val isPublic: Boolean? = false,
    @SerializedName("ipVersion") val ipVersion: Int? = 4,
    @SerializedName("isWhitelisted") val isWhitelisted: Boolean? = false,
    @SerializedName("abuseConfidenceScore") val abuseConfidenceScore: Int = 0,
    @SerializedName("countryCode") val countryCode: String? = null,
    @SerializedName("countryName") val countryName: String? = null,
    @SerializedName("usageType") val usageType: String? = null,
    @SerializedName("isp") val isp: String? = null,
    @SerializedName("domain") val domain: String? = null,
    @SerializedName("hostnames") val hostnames: List<String>? = emptyList(),
    @SerializedName("isTor") val isTor: Boolean? = false,
    @SerializedName("totalReports") val totalReports: Int = 0,
    @SerializedName("numDistinctUsers") val numDistinctUsers: Int = 0,
    @SerializedName("lastReportedAt") val lastReportedAt: String? = null,
    @SerializedName("reports") val reports: List<ReportItem>? = emptyList()
)

data class ReportItem(
    @SerializedName("reportedAt") val reportedAt: String,
    @SerializedName("comment") val comment: String? = "",
    @SerializedName("categories") val categories: List<Int> = emptyList(),
    @SerializedName("reporterId") val reporterId: Int = 0,
    @SerializedName("reporterCountryCode") val reporterCountryCode: String? = null,
    @SerializedName("reporterCountryName") val reporterCountryName: String? = null
)

// --- REPORTS PAGINATED ENDPOINT MODELS ---
data class ReportsResponse(
    @SerializedName("data") val data: ReportsData?
)

data class ReportsData(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("count") val count: Int = 0,
    @SerializedName("perPage") val perPage: Int = 25,
    @SerializedName("lastPage") val lastPage: Int = 1,
    @SerializedName("nextPageUrl") val nextPageUrl: String? = null,
    @SerializedName("previousPageUrl") val previousPageUrl: String? = null,
    @SerializedName("results") val results: List<ReportItem> = emptyList()
)

// --- BLACKLIST ENDPOINT MODELS ---
data class BlacklistResponse(
    @SerializedName("meta") val meta: BlacklistMeta?,
    @SerializedName("data") val data: List<BlacklistItem> = emptyList()
)

data class BlacklistMeta(
    @SerializedName("generatedAt") val generatedAt: String? = null
)

data class BlacklistItem(
    @SerializedName("ipAddress") val ipAddress: String,
    @SerializedName("countryCode") val countryCode: String? = null,
    @SerializedName("abuseConfidenceScore") val abuseConfidenceScore: Int = 0,
    @SerializedName("lastReportedAt") val lastReportedAt: String? = null
)

// --- CHECK-BLOCK ENDPOINT MODELS ---
data class CheckBlockResponse(
    @SerializedName("data") val data: CheckBlockData?
)

data class CheckBlockData(
    @SerializedName("networkAddress") val networkAddress: String? = null,
    @SerializedName("netmask") val netmask: String? = null,
    @SerializedName("minAddress") val minAddress: String? = null,
    @SerializedName("maxAddress") val maxAddress: String? = null,
    @SerializedName("numPossibleHosts") val numPossibleHosts: Long = 0,
    @SerializedName("addressSpaceDesc") val addressSpaceDesc: String? = null,
    @SerializedName("reportedAddress") val reportedAddress: List<ReportedAddress> = emptyList()
)

data class ReportedAddress(
    @SerializedName("ipAddress") val ipAddress: String,
    @SerializedName("numReports") val numReports: Int = 0,
    @SerializedName("mostRecentReport") val mostRecentReport: String? = null,
    @SerializedName("abuseConfidenceScore") val abuseConfidenceScore: Int = 0,
    @SerializedName("countryCode") val countryCode: String? = null
)

// --- REPORT SUBMISSION MODELS ---
data class ReportResponse(
    @SerializedName("data") val data: ReportResponseData?
)

data class ReportResponseData(
    @SerializedName("ipAddress") val ipAddress: String,
    @SerializedName("abuseConfidenceScore") val abuseConfidenceScore: Int = 0
)

// --- CLEAR ADDRESS MODELS ---
data class ClearAddressResponse(
    @SerializedName("data") val data: ClearAddressData?
)

data class ClearAddressData(
    @SerializedName("numReportsDeleted") val numReportsDeleted: Int = 0
)

// --- API ERROR MODEL ---
data class ApiErrorResponse(
    @SerializedName("errors") val errors: List<ApiErrorDetail>? = emptyList()
)

data class ApiErrorDetail(
    @SerializedName("detail") val detail: String,
    @SerializedName("status") val status: Int? = null
)

// --- API RATE LIMIT INFORMATION ---
data class RateLimitInfo(
    val limit: Int = 1000,
    val remaining: Int = 1000,
    val resetEpochSeconds: Long = 0,
    val retryAfterSeconds: Long = 0
)

// --- LOCAL DATA MODELS FOR BOOKMARKS & HISTORY ---
data class BookmarkedIp(
    val ipAddress: String,
    val abuseConfidenceScore: Int,
    val countryCode: String?,
    val isp: String?,
    val savedTimestamp: Long = System.currentTimeMillis()
)

data class SearchHistoryItem(
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

// --- ABUSE CATEGORIES ENUM / CONSTANTS ---
data class AbuseCategory(
    val id: Int,
    val name: String,
    val description: String
) {
    companion object {
        val ALL_CATEGORIES = listOf(
            AbuseCategory(3, "Fraud Orders", "Fraudulent orders / e-commerce fraud"),
            AbuseCategory(4, "DDoS Attack", "Distributed Denial of Service attacks"),
            AbuseCategory(5, "FTP Brute-Force", "FTP authentication brute-force attempts"),
            AbuseCategory(9, "Open Proxy", "Open proxy or compromised VPN endpoint"),
            AbuseCategory(10, "Web Spam", "Comment spam, forum spam, HTTP POST spam"),
            AbuseCategory(11, "Email Spam", "Unsolicited bulk email / spam relay"),
            AbuseCategory(14, "Port Scan", "Unauthorized port scanning & probing"),
            AbuseCategory(15, "Hacking", "Unauthorized system access attempts"),
            AbuseCategory(18, "SSH", "SSH service connections & unauthorized login"),
            AbuseCategory(19, "Bad Web Bot", "Malicious crawlers, scrapers, automated tools"),
            AbuseCategory(20, "Exploit Attempt", "Exploitation of known vulnerabilities (CVEs)"),
            AbuseCategory(21, "Web App Attack", "SQL injection, XSS, RFI/LFI web attacks"),
            AbuseCategory(22, "SSH Brute-Force", "Repeated SSH authentication failures"),
            AbuseCategory(23, "IoT Targeted", "Attacks targeting IoT devices & routers")
        )

        fun getCategoryName(id: Int): String {
            return ALL_CATEGORIES.find { it.id == id }?.name ?: "Category $id"
        }
    }
}
