package com.scriptforlil.kindroidhealthsync.ui

import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.scriptforlil.kindroidhealthsync.R
import com.scriptforlil.kindroidhealthsync.data.AppContainer
import com.scriptforlil.kindroidhealthsync.data.health.HealthSnapshot
import com.scriptforlil.kindroidhealthsync.data.local.SyncHistoryEntry
import com.scriptforlil.kindroidhealthsync.data.local.SyncStatusState
import com.scriptforlil.kindroidhealthsync.domain.MessageComposer
import com.scriptforlil.kindroidhealthsync.healthconnect.HealthConnectAvailability
import com.scriptforlil.kindroidhealthsync.sync.SyncScheduler
import com.scriptforlil.kindroidhealthsync.sync.SyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

data class SettingsState(
    val apiKey: String = "",
    val aiId: String = "",
    val kinName: String = "",
    val messageTemplate: String = "",
    val messageLanguage: String = "en",
    val appLanguage: String = "system",
    val syncIntervalMinutes: Int = 15,
    val quietHours: String = "",
    val includeSleep: Boolean = true,
    val includeActivity: Boolean = true,
    val includeCurrentHeartRate: Boolean = true,
    val includeAverageHeartRate: Boolean = true,
)

data class UiState(
    val settings: SettingsState = SettingsState(),
    val previewMessage: String = "",
    val status: String = "",
    val lastSyncAt: String = "",
    val autoSyncAt: String = "",
    val autoSyncStatus: String = "",
    val workerState: String = "",
    val isLoading: Boolean = true,
    val healthConnectAvailability: HealthConnectAvailability = HealthConnectAvailability.NotInstalled,
    val hasHealthPermissions: Boolean = false,
    val healthConnectMessage: String = "",
    val requiredPermissions: Set<String> = emptySet(),
    val errorDetails: String = "",
    val lastApiResponse: String = "",
    val syncHistory: List<SyncHistoryEntry> = emptyList(),
)

class MainViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val composer = MessageComposer(container.appContext)
    private val requiredPermissions = container.healthRepository.requiredPermissions
    private val workManager = WorkManager.getInstance(container.appContext)
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    private var lastSnapshot: HealthSnapshot? = null
    private var latestSettings = SettingsState()
    private var latestSyncStatus = SyncStatusState()
    private var settingsLoaded = false
    private var syncStatusLoaded = false
    private var startupSyncEvaluated = false

    private fun text(resId: Int, vararg args: Any): String = container.appContext.getString(resId, *args)

    private val workInfoObserver = Observer<List<WorkInfo>> { infos ->
        val state = pickRelevantWorkState(infos)
        _uiState.value = _uiState.value.copy(workerState = mapWorkState(state))
    }

    private val _uiState = MutableStateFlow(
        UiState(
            status = text(R.string.status_never_synced),
            lastSyncAt = text(R.string.status_none),
            autoSyncAt = text(R.string.status_none),
            autoSyncStatus = text(R.string.auto_sync_status_never),
            workerState = text(R.string.worker_state_idle),
            healthConnectMessage = text(R.string.health_connect_message_checking),
            requiredPermissions = requiredPermissions,
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        workManager.getWorkInfosByTagLiveData(SyncWorker.WORK_TAG).observeForever(workInfoObserver)

        viewModelScope.launch {
            container.settingsRepository.settings.collect { settings ->
                latestSettings = settings
                settingsLoaded = true
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    previewMessage = lastSnapshot?.let { composer.compose(it, settings) }.orEmpty()
                )
                maybeTriggerStartupSync()
            }
        }
        viewModelScope.launch {
            container.settingsRepository.syncStatus.collect { syncStatus ->
                latestSyncStatus = syncStatus
                syncStatusLoaded = true
                _uiState.value = _uiState.value.copy(
                    lastSyncAt = syncStatus.lastManualSyncAt.ifBlank { text(R.string.status_none) },
                    autoSyncAt = syncStatus.lastAutoSyncAt.ifBlank { text(R.string.status_none) },
                    autoSyncStatus = syncStatus.lastAutoSyncStatus.ifBlank { text(R.string.auto_sync_status_never) },
                    syncHistory = syncStatus.syncHistory,
                )
                maybeTriggerStartupSync()
            }
        }
        viewModelScope.launch {
            container.settingsRepository.lastKinResponse.collect { response ->
                _uiState.value = _uiState.value.copy(lastApiResponse = response)
            }
        }
        refreshHealthConnectState()
    }

    override fun onCleared() {
        workManager.getWorkInfosByTagLiveData(SyncWorker.WORK_TAG).removeObserver(workInfoObserver)
        super.onCleared()
    }

    fun permissionContract(): ActivityResultContract<Set<String>, Set<String>> {
        return container.healthRepository.permissionContract()
    }

    fun updateSyncInterval(intervalMinutes: Int) {
        viewModelScope.launch {
            val updated = uiState.value.settings.copy(syncIntervalMinutes = intervalMinutes)
            container.settingsRepository.saveSettings(updated)
            SyncScheduler.schedule(container.appContext, intervalMinutes)
        }
    }

    fun updateSettings(transform: (SettingsState) -> SettingsState) {
        viewModelScope.launch {
            val current = uiState.value.settings
            val updated = transform(current)
            container.settingsRepository.saveSettings(updated)
            SyncScheduler.schedule(container.appContext, updated.syncIntervalMinutes)
        }
    }

    fun refreshHealthConnectState() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val availability = container.healthRepository.getAvailability()

            if (availability != HealthConnectAvailability.Available) {
                lastSnapshot = null
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    healthConnectAvailability = availability,
                    hasHealthPermissions = false,
                    previewMessage = "",
                    errorDetails = "",
                    healthConnectMessage = when (availability) {
                        HealthConnectAvailability.NotInstalled -> text(R.string.health_connect_message_install)
                        HealthConnectAvailability.UpdateRequired -> text(R.string.health_connect_message_update)
                        HealthConnectAvailability.NotSupported -> text(R.string.health_connect_message_not_supported)
                        HealthConnectAvailability.Available -> text(R.string.health_connect_message_available)
                    }
                )
                return@launch
            }

            val hasPermissions = container.healthRepository.hasAllPermissions()
            _uiState.value = _uiState.value.copy(
                healthConnectAvailability = availability,
                hasHealthPermissions = hasPermissions,
                healthConnectMessage = if (hasPermissions) {
                    text(R.string.health_connect_message_ready)
                } else {
                    text(R.string.health_connect_message_grant)
                }
            )

            if (!hasPermissions) {
                lastSnapshot = null
                _uiState.value = _uiState.value.copy(isLoading = false, previewMessage = "", errorDetails = "")
                return@launch
            }

            refreshPreviewInternal()
        }
    }

    fun onPermissionsGranted(grantedPermissions: Set<String>) {
        val grantedAll = grantedPermissions.containsAll(requiredPermissions)
        _uiState.value = _uiState.value.copy(hasHealthPermissions = grantedAll)
        if (grantedAll) {
            SyncScheduler.schedule(container.appContext, uiState.value.settings.syncIntervalMinutes)
        }
        refreshHealthConnectState()
    }

    fun refreshPreview() {
        viewModelScope.launch {
            if (_uiState.value.healthConnectAvailability != HealthConnectAvailability.Available) {
                _uiState.value = _uiState.value.copy(status = text(R.string.status_health_connect_unavailable))
                return@launch
            }
            if (!_uiState.value.hasHealthPermissions) {
                _uiState.value = _uiState.value.copy(status = text(R.string.status_health_permissions_missing))
                return@launch
            }
            refreshPreviewInternal()
        }
    }

    private suspend fun refreshPreviewInternal() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        runCatching { container.healthRepository.getLatestSnapshot() }
            .onSuccess { snapshot ->
                lastSnapshot = snapshot
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    previewMessage = composer.compose(snapshot, _uiState.value.settings),
                    status = text(R.string.status_preview_loaded),
                    healthConnectMessage = text(R.string.health_connect_message_loaded),
                    errorDetails = ""
                )
            }
            .onFailure {
                lastSnapshot = null
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    previewMessage = "",
                    status = text(R.string.status_health_read_error),
                    healthConnectMessage = text(R.string.health_connect_message_load_failed),
                    errorDetails = it.stackTraceToString()
                )
            }
    }

    fun sendTest() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val state = uiState.value
            val result = container.kindroidRepository.sendMessage(
                apiKey = state.settings.apiKey,
                aiId = state.settings.aiId,
                message = state.previewMessage
            )

            val nowMillis = System.currentTimeMillis()
            val formattedNow = LocalDateTime.now().format(timestampFormatter)
            val timedOut = result.getOrNull() == text(R.string.response_timeout_placeholder)
            val successfulResponse = result.getOrNull().orEmpty()
            if (successfulResponse.isNotBlank()) {
                container.settingsRepository.saveLastKinResponse(successfulResponse)
            }
            val manualStatus = when {
                timedOut -> text(R.string.status_send_timeout)
                result.isSuccess -> text(R.string.status_send_success)
                else -> text(R.string.status_send_error)
            }
            container.settingsRepository.saveManualSync(formattedNow, nowMillis, manualStatus)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                lastSyncAt = formattedNow,
                status = manualStatus,
                errorDetails = if (result.isSuccess) "" else result.exceptionOrNull()?.stackTraceToString().orEmpty(),
                lastApiResponse = successfulResponse.ifBlank { _uiState.value.lastApiResponse }
            )
        }
    }

    private fun maybeTriggerStartupSync() {
        if (startupSyncEvaluated || !settingsLoaded || !syncStatusLoaded) return

        val intervalMinutes = latestSettings.syncIntervalMinutes.coerceAtLeast(15)
        SyncScheduler.schedule(container.appContext, intervalMinutes)
        startupSyncEvaluated = true
    }

    private fun pickRelevantWorkState(infos: List<WorkInfo>): WorkInfo.State? {
        return infos
            .sortedByDescending { workInfoPriority(it.state) }
            .firstOrNull()
            ?.state
    }

    private fun workInfoPriority(state: WorkInfo.State): Int = when (state) {
        WorkInfo.State.RUNNING -> 6
        WorkInfo.State.ENQUEUED -> 5
        WorkInfo.State.SUCCEEDED -> 4
        WorkInfo.State.BLOCKED -> 3
        WorkInfo.State.FAILED -> 2
        WorkInfo.State.CANCELLED -> 1
    }

    private fun mapWorkState(state: WorkInfo.State?): String = when (state) {
        WorkInfo.State.ENQUEUED -> text(R.string.worker_state_enqueued)
        WorkInfo.State.RUNNING -> text(R.string.worker_state_running)
        WorkInfo.State.SUCCEEDED -> text(R.string.worker_state_succeeded)
        WorkInfo.State.FAILED -> text(R.string.worker_state_failed)
        WorkInfo.State.BLOCKED -> text(R.string.worker_state_blocked)
        WorkInfo.State.CANCELLED -> text(R.string.worker_state_cancelled)
        null -> text(R.string.worker_state_idle)
    }
}

class MainViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(container) as T
    }
}







