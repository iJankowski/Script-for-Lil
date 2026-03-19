package com.scriptforlil.kindroidhealthsync.data.local

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.scriptforlil.kindroidhealthsync.ui.SettingsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(context: Context) {
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("settings.preferences_pb") }
    )

    val settings: Flow<SettingsState> = dataStore.data.map { prefs ->
        SettingsState(
            apiKey = prefs[Keys.ApiKey].orEmpty(),
            aiId = prefs[Keys.AiId].orEmpty(),
            syncIntervalMinutes = prefs[Keys.SyncIntervalMinutes] ?: 15,
            quietHours = prefs[Keys.QuietHours].orEmpty(),
            includeSleep = prefs[Keys.IncludeSleep] ?: true,
            includeActivity = prefs[Keys.IncludeActivity] ?: true,
            includeCurrentHeartRate = prefs[Keys.IncludeCurrentHeartRate] ?: true,
            includeAverageHeartRate = prefs[Keys.IncludeAverageHeartRate] ?: true,
        )
    }

    suspend fun saveSettings(state: SettingsState) {
        dataStore.edit { prefs ->
            prefs[Keys.ApiKey] = state.apiKey
            prefs[Keys.AiId] = state.aiId
            prefs[Keys.SyncIntervalMinutes] = state.syncIntervalMinutes
            prefs[Keys.QuietHours] = state.quietHours
            prefs[Keys.IncludeSleep] = state.includeSleep
            prefs[Keys.IncludeActivity] = state.includeActivity
            prefs[Keys.IncludeCurrentHeartRate] = state.includeCurrentHeartRate
            prefs[Keys.IncludeAverageHeartRate] = state.includeAverageHeartRate
        }
    }

    private object Keys {
        val ApiKey = stringPreferencesKey("api_key")
        val AiId = stringPreferencesKey("ai_id")
        val SyncIntervalMinutes = intPreferencesKey("sync_interval_minutes")
        val QuietHours = stringPreferencesKey("quiet_hours")
        val IncludeSleep = booleanPreferencesKey("include_sleep")
        val IncludeActivity = booleanPreferencesKey("include_activity")
        val IncludeCurrentHeartRate = booleanPreferencesKey("include_current_heart_rate")
        val IncludeAverageHeartRate = booleanPreferencesKey("include_average_heart_rate")
    }
}
