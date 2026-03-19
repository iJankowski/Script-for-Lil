package com.scriptforlil.kindroidhealthsync.healthconnect

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object HealthConnectNavigation {
    fun openHealthConnect(context: Context): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(
            HealthConnectAvailabilityChecker.ProviderPackageName
        )
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            return true
        }

        val detailsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${HealthConnectAvailabilityChecker.ProviderPackageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(detailsIntent)
        return false
    }

    fun openManagePermissions(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val intent = Intent("android.health.connect.action.MANAGE_HEALTH_PERMISSIONS")
                .putExtra("android.intent.extra.PACKAGE_NAME", context.packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return true
        }

        return openHealthConnect(context)
    }
}
