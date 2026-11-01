package com.example.sentinel_ip.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sentinel_ip.data.local.PreferencesManager
import com.example.sentinel_ip.data.model.BlacklistItem
import com.example.sentinel_ip.data.model.BookmarkedIp
import com.example.sentinel_ip.data.model.CheckBlockData
import com.example.sentinel_ip.data.model.CheckData
import com.example.sentinel_ip.data.model.ReportResponseData
import com.example.sentinel_ip.data.repository.ApiResult
import com.example.sentinel_ip.data.repository.AbuseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class SentinelViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application.applicationContext)
    private val repository = AbuseRepository(preferencesManager)

    val apiKey: StateFlow<String> = repository.apiKeyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val bookmarks: StateFlow<List<BookmarkedIp>> = repository.bookmarksFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val searchHistory = repository.searchHistoryFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val rateLimitInfo = repository.rateLimitInfoFlow

    // --- CHECK STATE ---
    private val _checkState = MutableStateFlow<UiState<CheckData>>(UiState.Idle)
    val checkState: StateFlow<UiState<CheckData>> = _checkState.asStateFlow()

    // --- BLACKLIST STATE ---
    private val _blacklistState = MutableStateFlow<UiState<List<BlacklistItem>>>(UiState.Idle)
    val blacklistState: StateFlow<UiState<List<BlacklistItem>>> = _blacklistState.asStateFlow()

    // --- SUBNET STATE ---
    private val _subnetState = MutableStateFlow<UiState<CheckBlockData>>(UiState.Idle)
    val subnetState: StateFlow<UiState<CheckBlockData>> = _subnetState.asStateFlow()

    // --- REPORT SUBMISSION STATE ---
    private val _reportState = MutableStateFlow<UiState<ReportResponseData>>(UiState.Idle)
    val reportState: StateFlow<UiState<ReportResponseData>> = _reportState.asStateFlow()

    init {
        viewModelScope.launch {
            apiKey.collect { key ->
                if (key.isNotBlank()) {
                    // Pre-load default blacklist when key is loaded
                    fetchBlacklist()
                }
            }
        }
    }

    fun saveApiKey(newKey: String) {
        viewModelScope.launch {
            repository.saveApiKey(newKey)
            _checkState.value = UiState.Idle
            _blacklistState.value = UiState.Idle
            fetchBlacklist()
        }
    }

    fun auditIp(ipAddress: String, maxAgeInDays: Int = 30) {
        if (ipAddress.isBlank()) return
        viewModelScope.launch {
            _checkState.value = UiState.Loading
            repository.addSearchHistory(ipAddress)
            when (val result = repository.checkIp(ipAddress.trim(), maxAgeInDays)) {
                is ApiResult.Success -> _checkState.value = UiState.Success(result.data)
                is ApiResult.Error -> _checkState.value = UiState.Error(result.message)
                ApiResult.Loading -> _checkState.value = UiState.Loading
            }
        }
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
            repository.addSearchHistory(network)
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
            val item = BookmarkedIp(
                ipAddress = checkData.ipAddress,
                abuseConfidenceScore = checkData.abuseConfidenceScore,
                countryCode = checkData.countryCode,
                isp = checkData.isp
            )
            repository.toggleBookmark(item)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
