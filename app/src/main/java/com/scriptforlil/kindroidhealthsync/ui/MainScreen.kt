package com.scriptforlil.kindroidhealthsync.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scriptforlil.kindroidhealthsync.healthconnect.HealthConnectAvailability

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = viewModel.permissionContract()
    ) { granted ->
        viewModel.onPermissionsGranted(granted)
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceContainerLowest,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.32f)
                        )
                    )
                )
                .safeDrawingPadding()
                .navigationBarsPadding()
        ) {
            if (uiState.isLoading && uiState.previewMessage.isBlank()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeaderCard()
                    HealthConnectCard(
                        availability = uiState.healthConnectAvailability,
                        message = uiState.healthConnectMessage,
                        hasPermissions = uiState.hasHealthPermissions,
                        onRequestPermissions = { permissionsLauncher.launch(uiState.requiredPermissions) },
                        onRefresh = viewModel::refreshHealthConnectState,
                    )
                    SettingsCard(
                        state = uiState.settings,
                        onApiKeyChange = { value -> viewModel.updateSettings { it.copy(apiKey = value) } },
                        onAiIdChange = { value -> viewModel.updateSettings { it.copy(aiId = value) } },
                        onQuietHoursChange = { value -> viewModel.updateSettings { it.copy(quietHours = value) } },
                        onIntervalChange = { value -> viewModel.updateSettings { it.copy(syncIntervalMinutes = value) } },
                        onIncludeSleepChange = { value -> viewModel.updateSettings { it.copy(includeSleep = value) } },
                        onIncludeActivityChange = { value -> viewModel.updateSettings { it.copy(includeActivity = value) } },
                        onIncludeCurrentHeartRateChange = { value -> viewModel.updateSettings { it.copy(includeCurrentHeartRate = value) } },
                        onIncludeAverageHeartRateChange = { value -> viewModel.updateSettings { it.copy(includeAverageHeartRate = value) } },
                    )
                    PreviewCard(
                        previewMessage = uiState.previewMessage,
                        status = uiState.status,
                        lastSyncAt = uiState.lastSyncAt,
                        isLoading = uiState.isLoading,
                        onRefresh = viewModel::refreshPreview,
                        onSendTest = viewModel::sendTest
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeaderCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Kindroid Health Sync",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Lokalny szkic MVP oparty o Health Connect i jednego Kindroida. Huawei wpada przez Health Sync, my czytamy tylko oficjalny magazyn zdrowia Androida.",
                style = MaterialTheme.typography.bodyLarge
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = {}, label = { Text("Health Connect") })
                AssistChip(onClick = {}, label = { Text("15 min+") })
                AssistChip(onClick = {}, label = { Text("Jeden Kindroid") })
            }
        }
    }
}

@Composable
private fun HealthConnectCard(
    availability: HealthConnectAvailability,
    message: String,
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit,
    onRefresh: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Health Connect", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(message, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = when (availability) {
                    HealthConnectAvailability.Available -> if (hasPermissions) "Status: gotowy" else "Status: czeka na zgody"
                    HealthConnectAvailability.NotInstalled -> "Status: brak aplikacji lub modułu"
                    HealthConnectAvailability.UpdateRequired -> "Status: wymaga aktualizacji"
                    HealthConnectAvailability.NotSupported -> "Status: brak wsparcia"
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onRefresh, contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)) {
                    Text("Sprawdź")
                }
                if (availability == HealthConnectAvailability.Available && !hasPermissions) {
                    Button(onClick = onRequestPermissions, contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)) {
                        Text("Nadaj dostęp")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    state: SettingsState,
    onApiKeyChange: (String) -> Unit,
    onAiIdChange: (String) -> Unit,
    onQuietHoursChange: (String) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onIncludeSleepChange: (Boolean) -> Unit,
    onIncludeActivityChange: (Boolean) -> Unit,
    onIncludeCurrentHeartRateChange: (Boolean) -> Unit,
    onIncludeAverageHeartRateChange: (Boolean) -> Unit,
) {
    Card(shape = RoundedCornerShape(28.dp)) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Ustawienia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = state.apiKey,
                onValueChange = onApiKeyChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Kindroid API key") },
                singleLine = true
            )
            OutlinedTextField(
                value = state.aiId,
                onValueChange = onAiIdChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Kindroid ai_id") },
                singleLine = true
            )
            IntervalChipRow(
                selectedValue = state.syncIntervalMinutes,
                onSelected = onIntervalChange
            )
            OutlinedTextField(
                value = state.quietHours,
                onValueChange = onQuietHoursChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Godziny ciszy, np. 23:00-07:00") },
                singleLine = true
            )
            HorizontalDivider()
            ToggleRow("Sen", state.includeSleep, onIncludeSleepChange)
            ToggleRow("Aktywność", state.includeActivity, onIncludeActivityChange)
            ToggleRow("Tętno teraz", state.includeCurrentHeartRate, onIncludeCurrentHeartRateChange)
            ToggleRow("Średnie tętno 15 min", state.includeAverageHeartRate, onIncludeAverageHeartRateChange)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntervalChipRow(
    selectedValue: Int,
    onSelected: (Int) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(15, 30, 60).forEach { minutes ->
            AssistChip(
                onClick = { onSelected(minutes) },
                label = { Text("$minutes min") },
                enabled = selectedValue != minutes
            )
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PreviewCard(
    previewMessage: String,
    status: String,
    lastSyncAt: String,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onSendTest: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Podgląd i status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Ostatnia synchronizacja: $lastSyncAt", style = MaterialTheme.typography.bodyMedium)
            Text(status, style = MaterialTheme.typography.bodyMedium)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = if (previewMessage.isBlank()) "Brak podglądu wiadomości." else previewMessage,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Text("Odśwież podgląd")
                }
                Button(
                    onClick = onSendTest,
                    enabled = !isLoading,
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Text("Wyślij test")
                }
            }
            if (isLoading) {
                Spacer(modifier = Modifier.height(4.dp))
                CircularProgressIndicator()
            }
        }
    }
}
