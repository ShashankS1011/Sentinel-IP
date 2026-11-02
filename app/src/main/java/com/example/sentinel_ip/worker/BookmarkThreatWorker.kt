package com.example.sentinel_ip.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.sentinel_ip.data.local.PreferencesManager
import com.example.sentinel_ip.data.repository.AbuseRepository
import com.example.sentinel_ip.data.repository.ApiResult
import com.example.sentinel_ip.util.NotificationHelper
import kotlinx.coroutines.flow.first

class BookmarkThreatWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            val preferencesManager = PreferencesManager(context)
            val repository = AbuseRepository(context, preferencesManager)

            val bookmarks = repository.roomBookmarksFlow.first()
            bookmarks.forEach { bookmark ->
                when (val result = repository.checkIp(bookmark.ipAddress)) {
                    is ApiResult.Success -> {
                        if (result.data.abuseConfidenceScore >= 75) {
                            NotificationHelper.sendThreatAlert(context, bookmark.ipAddress, result.data.abuseConfidenceScore)
                        }
                    }
                    else -> {}
                }
            }
            ListenableWorker.Result.success()
        } catch (_: Exception) {
            ListenableWorker.Result.retry()
        }
    }
}
