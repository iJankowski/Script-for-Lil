package com.scriptforlil.kindroidhealthsync.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.scriptforlil.kindroidhealthsync.R
import com.scriptforlil.kindroidhealthsync.data.health.HealthRepository
import com.scriptforlil.kindroidhealthsync.data.local.SettingsRepository
import com.scriptforlil.kindroidhealthsync.data.remote.KindroidRepository
import com.scriptforlil.kindroidhealthsync.domain.MessageComposer
import com.scriptforlil.kindroidhealthsync.healthconnect.HealthConnectAvailability
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    private val appContext = appContext.applicationContext
    private val settingsRepository = SettingsRepository(appContext)
    private val healthRepository = HealthRepository(appContext)
    private val kindroidRepository = KindroidRepository(appContext)
    private val messageComposer = MessageComposer(appContext)
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    override suspend fun doWork(): Result {
        val settings = settingsRepository.settings.first()

        if (settings.apiKey.isBlank() || settings.aiId.isBlank()) {
            saveStatus(appContext.getString(R.string.auto_sync_status_skipped_missing_credentials))
            return Result.success()
        }

        if (isWithinQuietHours(settings.quietHours)) {
            saveStatus(appContext.getString(R.string.auto_sync_status_skipped_quiet_hours))
            return Result.success()
        }

        if (healthRepository.getAvailability() != HealthConnectAvailability.Available) {
            saveStatus(appContext.getString(R.string.auto_sync_status_skipped_health_connect_unavailable))
            return Result.success()
        }

        if (!healthRepository.hasAllPermissions()) {
            saveStatus(appContext.getString(R.string.auto_sync_status_skipped_permissions))
            return Result.success()
        }

        return runCatching {
            val snapshot = healthRepository.getLatestSnapshot()
            val message = messageComposer.compose(snapshot, settings)
            val result = kindroidRepository.sendMessage(
                apiKey = settings.apiKey,
                aiId = settings.aiId,
                message = message,
            )

            if (result.isSuccess) {
                val response = result.getOrNull().orEmpty()
                if (response.isNotBlank()) {
                    settingsRepository.saveLastKinResponse(response)
                }
                val status = if (response == appContext.getString(R.string.response_timeout_placeholder)) {
                    appContext.getString(R.string.auto_sync_status_sent_timeout)
                } else {
                    appContext.getString(R.string.auto_sync_status_sent)
                }
                saveStatus(status)
            } else {
                saveStatus(appContext.getString(R.string.auto_sync_status_kindroid_error))
            }
        }.fold(
            onSuccess = { Result.success() },
            onFailure = {
                saveStatus(appContext.getString(R.string.auto_sync_status_worker_error))
                Result.success()
            }
        )
    }

    private suspend fun saveStatus(status: String) {
        val now = System.currentTimeMillis()
        val at = LocalDateTime.now().format(timestampFormatter)
        settingsRepository.saveAutoSyncStatus(at, now, status)
    }

    private fun isWithinQuietHours(quietHours: String): Boolean {
        if (quietHours.isBlank() || !quietHours.contains('-')) return false

        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val parts = quietHours.split('-')
        if (parts.size != 2) return false

        return runCatching {
            val start = LocalTime.parse(parts[0].trim(), formatter)
            val end = LocalTime.parse(parts[1].trim(), formatter)
            val now = LocalTime.now()

            if (start <= end) {
                now >= start && now < end
            } else {
                now >= start || now < end
            }
        }.getOrDefault(false)
    }

    companion object {
        const val WORK_NAME = "kindroid-health-sync"
        const val WORK_TAG = "kindroid-health-sync-worker"
    }
}


