package com.example.sentinel_ip.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sentinel_ip.data.local.PreferencesManager
import com.example.sentinel_ip.data.local.room.BookmarkedIpEntity
import com.example.sentinel_ip.data.local.room.SearchHistoryEntity
import com.example.sentinel_ip.data.model.BlacklistItem
import com.example.sentinel_ip.data.model.BookmarkedIp
import com.example.sentinel_ip.data.model.CheckBlockData
import com.example.sentinel_ip.data.model.CheckData
import com.example.sentinel_ip.data.model.ReportResponseData
import com.example.sentinel_ip.data.repository.AbuseRepository
import com.example.sentinel_ip.data.repository.ApiResult
import com.example.sentinel_ip.data.repository.BulkAuditResult
import com.example.sentinel_ip.data.repository.ResolvedTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

enum class BookmarkSortOption {
    RECENT, SCORE_DESC, COUNTRY
}

class SentinelViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application.applicationContext)
    private val repository = AbuseRepository(application.applicationContext, preferencesManager)

    val apiKey: StateFlow<String> = repository.apiKeyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val rateLimitInfo = repository.rateLimitInfoFlow

    // --- SORTING & ROOM BOOKMARKS ---
    private val _bookmarkSortOption = MutableStateFlow(BookmarkSortOption.RECENT)
    val bookmarkSortOption: StateFlow<BookmarkSortOption> = _bookmarkSortOption.asStateFlow()

    private val _bookmarkSearchQuery = MutableStateFlow("")
    val bookmarkSearchQuery: StateFlow<String> = _bookmarkSearchQuery.asStateFlow()

    val roomBookmarks: StateFlow<List<BookmarkedIp>> = combine(
        repository.roomBookmarksFlow,
        _bookmarkSortOption,
        _bookmarkSearchQuery
    ) { list, sortOption, search ->
        var filtered = if (search.isNotBlank()) {
            list.filter {
                it.ipAddress.contains(search, ignoreCase = true) ||
                it.isp?.contains(search, ignoreCase = true) == true ||
                it.countryCode?.contains(search, ignoreCase = true) == true
            }
        } else {
            list
        }

        when (sortOption) {
            BookmarkSortOption.RECENT -> filtered
            BookmarkSortOption.SCORE_DESC -> filtered.sortedByDescending { it.abuseConfidenceScore }
            BookmarkSortOption.COUNTRY -> filtered.sortedBy { it.countryCode ?: "ZZ" }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val searchHistory: StateFlow<List<SearchHistoryEntity>> = repository.roomSearchHistoryFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- CHECK STATE ---
    private val _checkState = MutableStateFlow<UiState<CheckData>>(UiState.Idle)
    val checkState: StateFlow<UiState<CheckData>> = _checkState.asStateFlow()

    private val _resolvedTargetBanner = MutableStateFlow<ResolvedTarget?>(null)
    val resolvedTargetBanner: StateFlow<ResolvedTarget?> = _resolvedTargetBanner.asStateFlow()

    // --- BLACKLIST STATE ---
    private val _blacklistState = MutableStateFlow<UiState<List<BlacklistItem>>>(UiState.Idle)
    val blacklistState: StateFlow<UiState<List<BlacklistItem>>> = _blacklistState.asStateFlow()

    // --- SUBNET STATE ---
    private val _subnetState = MutableStateFlow<UiState<CheckBlockData>>(UiState.Idle)
    val subnetState: StateFlow<UiState<CheckBlockData>> = _subnetState.asStateFlow()

    // --- BULK AUDIT STATE ---
    private val _bulkAuditState = MutableStateFlow<UiState<BulkAuditResult>>(UiState.Idle)
    val bulkAuditState: StateFlow<UiState<BulkAuditResult>> = _bulkAuditState.asStateFlow()

    // --- REPORT SUBMISSION STATE ---
    private val _reportState = MutableStateFlow<UiState<ReportResponseData>>(UiState.Idle)
    val reportState: StateFlow<UiState<ReportResponseData>> = _reportState.asStateFlow()

    init {
        viewModelScope.launch {
            apiKey.collect { key ->
                if (key.isNotBlank()) {
                    fetchBlacklist()
                }
            }
        }
    }

    fun setBookmarkSortOption(option: BookmarkSortOption) {
        _bookmarkSortOption.value = option
    }

    fun setBookmarkSearchQuery(query: String) {
        _bookmarkSearchQuery.value = query
    }

    fun saveApiKey(newKey: String) {
        viewModelScope.launch {
            repository.saveApiKey(newKey)
            _checkState.value = UiState.Idle
            _blacklistState.value = UiState.Idle
            fetchBlacklist()
        }
    }

    fun auditIp(input: String, maxAgeInDays: Int = 30) {
        if (input.isBlank()) return
        viewModelScope.launch {
            _checkState.value = UiState.Loading
            val target = repository.resolveTarget(input)
            _resolvedTargetBanner.value = if (target.isDomainResolved) target else null

            repository.addSearchHistory(
                query = target.query,
                resolvedIp = if (target.isDomainResolved) target.resolvedIp else null
            )

            when (val result = repository.checkIp(target.resolvedIp, maxAgeInDays)) {
                is ApiResult.Success -> _checkState.value = UiState.Success(result.data)
                is ApiResult.Error -> _checkState.value = UiState.Error(result.message)
                ApiResult.Loading -> _checkState.value = UiState.Loading
            }
        }
    }

    fun runBulkAudit(rawLogText: String) {
        if (rawLogText.isBlank()) return
        viewModelScope.launch {
            _bulkAuditState.value = UiState.Loading
            when (val result = repository.auditBulkIps(rawLogText)) {
                is ApiResult.Success -> _bulkAuditState.value = UiState.Success(result.data)
                is ApiResult.Error -> _bulkAuditState.value = UiState.Error(result.message)
                ApiResult.Loading -> _bulkAuditState.value = UiState.Loading
            }
        }
    }

    fun resetBulkAuditState() {
        _bulkAuditState.value = UiState.Idle
    }

    fun fetchBlacklist(
        confidenceMinimum: Int = 90,
        limit: Int = 1000,
        onlyCountries: String? = null,
        ipVersion: Int? = null
    ) {
        viewModelScope.launch {
            _blacklistState.value = UiState.Loading
            when (val result = repository.getBlacklist(
                confidenceMinimum = confidenceMinimum,
                limit = limit,
                onlyCountries = onlyCountries,
                ipVersion = ipVersion
            )) {
                is ApiResult.Success -> _blacklistState.value = UiState.Success(result.data.data)
                is ApiResult.Error -> _blacklistState.value = UiState.Error(result.message)
                ApiResult.Loading -> _blacklistState.value = UiState.Loading
            }
        }
    }

    fun scanSubnet(network: String, maxAgeInDays: Int = 30) {
        if (network.isBlank()) return
        viewModelScope.launch {
            _subnetState.value = UiState.Loading
            repository.addSearchHistory(network, null)
            when (val result = repository.checkBlock(network.trim(), maxAgeInDays)) {
                is ApiResult.Success -> {
                    val data = result.data.data
                    if (data != null) {
                        _subnetState.value = UiState.Success(data)
                    } else {
                        _subnetState.value = UiState.Error("No subnet data returned")
                    }
                }
                is ApiResult.Error -> _subnetState.value = UiState.Error(result.message)
                ApiResult.Loading -> _subnetState.value = UiState.Loading
            }
        }
    }

    fun reportIp(ip: String, categories: List<Int>, comment: String) {
        if (ip.isBlank()) return
        viewModelScope.launch {
            _reportState.value = UiState.Loading
            when (val result = repository.reportIp(ip.trim(), categories, comment)) {
                is ApiResult.Success -> {
                    val data = result.data.data
                    if (data != null) {
                        _reportState.value = UiState.Success(data)
                    } else {
                        _reportState.value = UiState.Error("Report saved successfully")
                    }
                }
                is ApiResult.Error -> _reportState.value = UiState.Error(result.message)
                ApiResult.Loading -> _reportState.value = UiState.Loading
            }
        }
    }

    fun resetReportState() {
        _reportState.value = UiState.Idle
    }

    fun toggleBookmark(checkData: CheckData) {
        viewModelScope.launch {
            val entity = BookmarkedIpEntity(
                ipAddress = checkData.ipAddress,
                abuseConfidenceScore = checkData.abuseConfidenceScore,
                countryCode = checkData.countryCode,
                isp = checkData.isp,
                domain = checkData.domain
            )
            repository.toggleBookmark(entity)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun generateShareableReport(data: CheckData): String {
        val level = when {
            data.abuseConfidenceScore < 25 -> "Safe / Low Risk"
            data.abuseConfidenceScore < 75 -> "Suspicious"
            else -> "Critical Threat / Malicious"
        }
        return """
            Sentinel-IP Threat Intelligence Briefing
            -----------------------------------------
            Target IP: ${data.ipAddress}
            Abuse Confidence Score: ${data.abuseConfidenceScore}% ($level)
            Country: ${data.countryName ?: "Unknown"} (${data.countryCode ?: "N/A"})
            ISP: ${data.isp ?: "N/A"}
            Domain: ${data.domain ?: "N/A"}
            Usage Type: ${data.usageType ?: "Unspecified"}
            Tor Exit Node: ${if (data.isTor == true) "YES" else "No"}
            Whitelisted: ${if (data.isWhitelisted == true) "YES" else "No"}
            Total Reports: ${data.totalReports} (${data.numDistinctUsers} distinct users)
            
            Audited via Sentinel-IP Android App
            AbuseIPDB Reference: https://www.abuseipdb.com/check/${data.ipAddress}
        """.trimIndent()
    }
}
