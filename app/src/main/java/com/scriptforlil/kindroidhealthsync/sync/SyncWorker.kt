package com.scriptforlil.kindroidhealthsync.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.scriptforlil.kindroidhealthsync.data.health.HealthRepository
import com.scriptforlil.kindroidhealthsync.data.local.SettingsRepository
import com.scriptforlil.kindroidhealthsync.data.remote.KindroidRepository
import com.scriptforlil.kindroidhealthsync.domain.MessageComposer
import com.scriptforlil.kindroidhealthsync.healthconnect.HealthConnectAvailability
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    private val settingsRepository = SettingsRepository(appContext)
    private val healthRepository = HealthRepository(appContext)
    private val kindroidRepository = KindroidRepository()
    private val messageComposer = MessageComposer()

    override suspend fun doWork(): Result {
        val settings = settingsRepository.settings.first()

        if (settings.apiKey.isBlank() || settings.aiId.isBlank()) {
            return Result.success()
        }

        if (isWithinQuietHours(settings.quietHours)) {
            return Result.success()
        }

        if (healthRepository.getAvailability() != HealthConnectAvailability.Available) {
            return Result.retry()
        }

        if (!healthRepository.hasAllPermissions()) {
            return Result.success()
        }

        return runCatching {
            val snapshot = healthRepository.getLatestSnapshot()
            val message = messageComposer.compose(snapshot, settings)
            kindroidRepository.sendMessage(
                apiKey = settings.apiKey,
                aiId = settings.aiId,
                message = message,
            ).getOrThrow()
        }.fold(
            onSuccess = { Result.success() },
            onFailure = {
                if (it is IOException) Result.retry() else Result.failure()
            }
        )
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
    }
}
