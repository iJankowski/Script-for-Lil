package com.scriptforlil.kindroidhealthsync.healthconnect

import android.content.Context
import android.os.Build
import androidx.health.connect.client.HealthConnectClient

enum class HealthConnectAvailability {
    Available,
    NotInstalled,
    NotSupported,
    UpdateRequired,
}

object HealthConnectAvailabilityChecker {
    private const val ProviderPackageName = "com.google.android.apps.healthdata"

    fun getAvailability(context: Context): HealthConnectAvailability {
        val status = HealthConnectClient.getSdkStatus(context, ProviderPackageName)
        return when (status) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.Available
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectAvailability.UpdateRequired
            HealthConnectClient.SDK_UNAVAILABLE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    HealthConnectAvailability.NotInstalled
                } else {
                    HealthConnectAvailability.NotSupported
                }
            }
            else -> HealthConnectAvailability.NotSupported
        }
    }
}

