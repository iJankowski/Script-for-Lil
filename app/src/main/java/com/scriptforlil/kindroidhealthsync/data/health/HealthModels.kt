package com.scriptforlil.kindroidhealthsync.data.health

data class HealthSnapshot(
    val sleepSummary: String? = null,
    val activitySummary: String? = null,
    val currentHeartRate: Int? = null,
    val averageHeartRate15Min: Int? = null,
)
