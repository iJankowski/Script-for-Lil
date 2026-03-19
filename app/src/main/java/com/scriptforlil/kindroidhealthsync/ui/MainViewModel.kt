package com.scriptforlil.kindroidhealthsync.ui

import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.scriptforlil.kindroidhealthsync.data.AppContainer
import com.scriptforlil.kindroidhealthsync.data.health.HealthSnapshot
import com.scriptforlil.kindroidhealthsync.domain.MessageComposer
import com.scriptforlil.kindroidhealthsync.healthconnect.HealthConnectAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class SettingsState(
    val apiKey: String = "",
    val aiId: String = "",
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
    val status: String = "Nie wysłano jeszcze żadnej synchronizacji.",
    val lastSyncAt: String = "Brak",
    val isLoading: Boolean = true,
    val healthConnectAvailability: HealthConnectAvailability = HealthConnectAvailability.NotInstalled,
    val hasHealthPermissions: Boolean = false,
    val healthConnectMessage: String = "Sprawdzanie dostępności Health Connect...",
    val requiredPermissions: Set<String> = emptySet(),
)

class MainViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val composer = MessageComposer()
    private val requiredPermissions = container.healthRepository.requiredPermissions
    private var lastSnapshot: HealthSnapshot? = null

    private val _uiState = MutableStateFlow(UiState(requiredPermissions = requiredPermissions))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            container.settingsRepository.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    previewMessage = lastSnapshot?.let { composer.compose(it, settings) }.orEmpty()
                )
            }
        }
        refreshHealthConnectState()
    }

    fun permissionContract(): ActivityResultContract<Set<String>, Set<String>> {
        return container.healthRepository.permissionContract()
    }

    fun updateSettings(transform: (SettingsState) -> SettingsState) {
        viewModelScope.launch {
            val current = uiState.value.settings
            container.settingsRepository.saveSettings(transform(current))
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
                    healthConnectMessage = when (availability) {
                        HealthConnectAvailability.NotInstalled -> "Zainstaluj lub włącz Health Connect na telefonie."
                        HealthConnectAvailability.UpdateRequired -> "Health Connect wymaga aktualizacji."
                        HealthConnectAvailability.NotSupported -> "To urządzenie nie wspiera Health Connect."
                        HealthConnectAvailability.Available -> "Health Connect jest dostępny."
                    }
                )
                return@launch
            }

            val hasPermissions = container.healthRepository.hasAllPermissions()
            _uiState.value = _uiState.value.copy(
                healthConnectAvailability = availability,
                hasHealthPermissions = hasPermissions,
                healthConnectMessage = if (hasPermissions) {
                    "Health Connect jest gotowy. Odczytuję dane."
                } else {
                    "Nadaj dostęp do snu, kroków i tętna w Health Connect."
                }
            )

            if (!hasPermissions) {
                lastSnapshot = null
                _uiState.value = _uiState.value.copy(isLoading = false, previewMessage = "")
                return@launch
            }

            refreshPreviewInternal()
        }
    }

    fun onPermissionsGranted(grantedPermissions: Set<String>) {
        val grantedAll = grantedPermissions.containsAll(requiredPermissions)
        _uiState.value = _uiState.value.copy(hasHealthPermissions = grantedAll)
        refreshHealthConnectState()
    }

    fun refreshPreview() {
        viewModelScope.launch {
            if (_uiState.value.healthConnectAvailability != HealthConnectAvailability.Available) {
                _uiState.value = _uiState.value.copy(status = "Health Connect nie jest dostępny.")
                return@launch
            }
            if (!_uiState.value.hasHealthPermissions) {
                _uiState.value = _uiState.value.copy(status = "Brakuje zgód do odczytu danych zdrowotnych.")
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
                    status = "Podgląd bazuje na aktualnych danych z telefonu.",
                    healthConnectMessage = "Dane z Health Connect zostały odczytane."
                )
            }
            .onFailure {
                lastSnapshot = null
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    previewMessage = "",
                    status = "Błąd odczytu danych zdrowotnych: ${it.message}",
                    healthConnectMessage = "Nie udało się pobrać danych z Health Connect."
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

            val formattedNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                lastSyncAt = formattedNow,
                status = result.fold(
                    onSuccess = { "Testowa wiadomość została przygotowana i wysłana stubem." },
                    onFailure = { "Błąd testowej wysyłki: ${it.message}" }
                )
            )
        }
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
