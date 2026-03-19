package com.scriptforlil.kindroidhealthsync.data.health

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.scriptforlil.kindroidhealthsync.healthconnect.HealthConnectAvailability
import com.scriptforlil.kindroidhealthsync.healthconnect.HealthConnectAvailabilityChecker
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HealthRepository(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val client by lazy { HealthConnectClient.getOrCreate(appContext) }
    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val wakeUpFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val requiredPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
    )

    fun getAvailability(): HealthConnectAvailability {
        return HealthConnectAvailabilityChecker.getAvailability(appContext)
    }

    fun permissionContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract(
            HealthConnectAvailabilityChecker.ProviderPackageName
        )
    }

    suspend fun hasAllPermissions(): Boolean {
        if (getAvailability() != HealthConnectAvailability.Available) return false
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(requiredPermissions)
    }

    suspend fun getLatestSnapshot(): HealthSnapshot {
        check(getAvailability() == HealthConnectAvailability.Available) {
            "Health Connect nie jest dostępny na tym urządzeniu."
        }
        check(hasAllPermissions()) {
            "Brakuje zgód do odczytu danych z Health Connect."
        }

        val now = Instant.now()
        val startOfToday = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant()
        val fifteenMinutesAgo = now.minus(Duration.ofMinutes(15))
        val twoDaysAgo = now.minus(Duration.ofDays(2))
        val oneDayAgo = now.minus(Duration.ofDays(1))

        val stepsAggregate = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startOfToday, now)
            )
        )

        val heartRateRecords = client.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(oneDayAgo, now),
                ascendingOrder = false,
                pageSize = 20,
            )
        ).records

        val averageHeartRateAggregate = client.aggregate(
            AggregateRequest(
                metrics = setOf(HeartRateRecord.BPM_AVG),
                timeRangeFilter = TimeRangeFilter.between(fifteenMinutesAgo, now)
            )
        )

        val sleepSessions = client.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(twoDaysAgo, now),
                ascendingOrder = false,
                pageSize = 10,
            )
        ).records

        val latestHeartRate = heartRateRecords
            .flatMap { it.samples }
            .maxByOrNull { it.time }
            ?.beatsPerMinute
            ?.toInt()

        val latestSleep = sleepSessions.maxByOrNull { it.endTime }

        return HealthSnapshot(
            sleepSummary = latestSleep?.let { formatSleepSession(it.startTime, it.endTime) },
            activitySummary = stepsAggregate[StepsRecord.COUNT_TOTAL]?.toInt()?.let { "$it kroków" },
            currentHeartRate = latestHeartRate,
            averageHeartRate15Min = averageHeartRateAggregate[HeartRateRecord.BPM_AVG]?.toInt(),
        )
    }

    private fun formatSleepSession(start: Instant, end: Instant): String {
        val duration = Duration.between(start, end)
        val totalMinutes = duration.toMinutes().coerceAtLeast(0)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val wakeUpAt = end.atZone(zoneId).toLocalTime().format(wakeUpFormatter)
        return "$hours h $minutes min, pobudka o $wakeUpAt"
    }
}
