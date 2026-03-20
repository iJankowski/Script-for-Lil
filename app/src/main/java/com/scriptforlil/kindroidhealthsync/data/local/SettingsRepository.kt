package com.scriptforlil.kindroidhealthsync.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.scriptforlil.kindroidhealthsync.ui.SettingsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class SyncStatusState(
    val lastAutoSyncAt: String = "",
    val lastAutoSyncStatus: String = "",
    val lastAutoSyncAtMillis: Long = 0L,
    val lastManualSyncAt: String = "",
    val lastManualSyncAtMillis: Long = 0L,
)

class SettingsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = getDataStore(appContext)

    val settings: Flow<SettingsState> = dataStore.data.map { prefs ->
        SettingsState(
            apiKey = prefs[Keys.ApiKey].orEmpty(),
            aiId = prefs[Keys.AiId].orEmpty(),
            kinName = prefs[Keys.KinName].orEmpty(),
            messageTemplate = prefs[Keys.MessageTemplate].orEmpty(),
            messageLanguage = prefs[Keys.MessageLanguage].orEmpty().ifBlank { "en" },
            appLanguage = prefs[Keys.AppLanguage].orEmpty().ifBlank { "system" },
            syncIntervalMinutes = prefs[Keys.SyncIntervalMinutes] ?: 15,
            quietHours = prefs[Keys.QuietHours].orEmpty(),
            includeSleep = prefs[Keys.IncludeSleep] ?: true,
            includeActivity = prefs[Keys.IncludeActivity] ?: true,
            includeCurrentHeartRate = prefs[Keys.IncludeCurrentHeartRate] ?: true,
            includeAverageHeartRate = prefs[Keys.IncludeAverageHeartRate] ?: true,
        )
    }

    val syncStatus: Flow<SyncStatusState> = dataStore.data.map { prefs ->
        SyncStatusState(
            lastAutoSyncAt = prefs[Keys.LastAutoSyncAt].orEmpty(),
            lastAutoSyncStatus = prefs[Keys.LastAutoSyncStatus].orEmpty(),
            lastAutoSyncAtMillis = prefs[Keys.LastAutoSyncAtMillis] ?: 0L,
            lastManualSyncAt = prefs[Keys.LastManualSyncAt].orEmpty(),
            lastManualSyncAtMillis = prefs[Keys.LastManualSyncAtMillis] ?: 0L,
        )
    }

    val darkThemeEnabled: Flow<Boolean?> = dataStore.data.map { prefs -> prefs[Keys.DarkThemeEnabled] }
    val heroExpanded: Flow<Boolean?> = dataStore.data.map { prefs -> prefs[Keys.HeroExpanded] }
    val lastKinResponse: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.LastKinResponse].orEmpty() }

    suspend fun saveSettings(state: SettingsState) {
        dataStore.edit { prefs ->
            prefs[Keys.ApiKey] = state.apiKey
            prefs[Keys.AiId] = state.aiId
            prefs[Keys.KinName] = state.kinName
            prefs[Keys.MessageTemplate] = state.messageTemplate
            prefs[Keys.MessageLanguage] = state.messageLanguage
            prefs[Keys.AppLanguage] = state.appLanguage
            prefs[Keys.SyncIntervalMinutes] = state.syncIntervalMinutes
            prefs[Keys.QuietHours] = state.quietHours
            prefs[Keys.IncludeSleep] = state.includeSleep
            prefs[Keys.IncludeActivity] = state.includeActivity
            prefs[Keys.IncludeCurrentHeartRate] = state.includeCurrentHeartRate
            prefs[Keys.IncludeAverageHeartRate] = state.includeAverageHeartRate
        }
    }

    suspend fun saveAutoSyncStatus(at: String, atMillis: Long, status: String) {
        dataStore.edit { prefs ->
            prefs[Keys.LastAutoSyncAt] = at
            prefs[Keys.LastAutoSyncAtMillis] = atMillis
            prefs[Keys.LastAutoSyncStatus] = status
        }
    }

    suspend fun saveManualSync(at: String, atMillis: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.LastManualSyncAt] = at
            prefs[Keys.LastManualSyncAtMillis] = atMillis
        }
    }

    suspend fun saveDarkThemeEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.DarkThemeEnabled] = enabled }
    }

    suspend fun saveHeroExpanded(expanded: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.HeroExpanded] = expanded }
    }

    suspend fun saveLastKinResponse(response: String) {
        dataStore.edit { prefs -> prefs[Keys.LastKinResponse] = response }
    }

    private object Keys {
        val ApiKey = stringPreferencesKey("api_key")
        val AiId = stringPreferencesKey("ai_id")
        val KinName = stringPreferencesKey("kin_name")
        val MessageTemplate = stringPreferencesKey("message_template")
        val MessageLanguage = stringPreferencesKey("message_language")
        val AppLanguage = stringPreferencesKey("app_language")
        val SyncIntervalMinutes = intPreferencesKey("sync_interval_minutes")
        val QuietHours = stringPreferencesKey("quiet_hours")
        val IncludeSleep = booleanPreferencesKey("include_sleep")
        val IncludeActivity = booleanPreferencesKey("include_activity")
        val IncludeCurrentHeartRate = booleanPreferencesKey("include_current_heart_rate")
        val IncludeAverageHeartRate = booleanPreferencesKey("include_average_heart_rate")
        val LastAutoSyncAt = stringPreferencesKey("last_auto_sync_at")
        val LastAutoSyncStatus = stringPreferencesKey("last_auto_sync_status")
        val LastAutoSyncAtMillis = longPreferencesKey("last_auto_sync_at_millis")
        val LastManualSyncAt = stringPreferencesKey("last_manual_sync_at")
        val LastManualSyncAtMillis = longPreferencesKey("last_manual_sync_at_millis")
        val DarkThemeEnabled = booleanPreferencesKey("dark_theme_enabled")
        val HeroExpanded = booleanPreferencesKey("hero_expanded")
        val LastKinResponse = stringPreferencesKey("last_kin_response")
    }

    companion object {
        @Volatile
        private var dataStoreInstance: DataStore<Preferences>? = null

        private fun getDataStore(context: Context): DataStore<Preferences> {
            return dataStoreInstance ?: synchronized(this) {
                dataStoreInstance ?: PreferenceDataStoreFactory.create(
                    produceFile = { context.preferencesDataStoreFile("settings.preferences_pb") }
                ).also { dataStoreInstance = it }
            }
        }
    }
}
