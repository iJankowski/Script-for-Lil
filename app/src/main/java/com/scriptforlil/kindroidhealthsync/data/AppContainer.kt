package com.scriptforlil.kindroidhealthsync.data

import android.content.Context
import com.scriptforlil.kindroidhealthsync.data.health.HealthRepository
import com.scriptforlil.kindroidhealthsync.data.local.SettingsRepository
import com.scriptforlil.kindroidhealthsync.data.remote.KindroidRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val settingsRepository by lazy { SettingsRepository(appContext) }
    val healthRepository by lazy { HealthRepository(appContext) }
    val kindroidRepository by lazy { KindroidRepository() }
}
