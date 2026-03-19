package com.scriptforlil.kindroidhealthsync.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.scriptforlil.kindroidhealthsync.healthconnect.HealthConnectAvailability
import com.scriptforlil.kindroidhealthsync.healthconnect.HealthConnectNavigation

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
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
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
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
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HeroCard(uiState = uiState)
                    HealthConnectCard(
                        availability = uiState.healthConnectAvailability,
                        message = uiState.healthConnectMessage,
                        hasPermissions = uiState.hasHealthPermissions,
                        onRequestPermissions = { permissionsLauncher.launch(uiState.requiredPermissions) },
                        onRefresh = viewModel::refreshHealthConnectState,
                        onOpenHealthConnect = { HealthConnectNavigation.openHealthConnect(context) },
                    )
                    SettingsCard(
                        state = uiState.settings,
                        onApiKeyChange = { value -> viewModel.updateSettings { it.copy(apiKey = value) } },
                        onAiIdChange = { value -> viewModel.updateSettings { it.copy(aiId = value) } },
                        onQuietHoursChange = { value -> viewModel.updateSettings { it.copy(quietHours = value) } },
                        onIntervalChange = viewModel::updateSyncInterval,
                        onIncludeSleepChange = { value -> viewModel.updateSettings { it.copy(includeSleep = value) } },
                        onIncludeActivityChange = { value -> viewModel.updateSettings { it.copy(includeActivity = value) } },
                        onIncludeCurrentHeartRateChange = { value -> viewModel.updateSettings { it.copy(includeCurrentHeartRate = value) } },
                        onIncludeAverageHeartRateChange = { value -> viewModel.updateSettings { it.copy(includeAverageHeartRate = value) } },
                    )
                    PreviewCard(
                        context = context,
                        previewMessage = uiState.previewMessage,
                        status = uiState.status,
                        lastSyncAt = uiState.lastSyncAt,
                        isLoading = uiState.isLoading,
                        errorDetails = uiState.errorDetails,
                        lastApiResponse = uiState.lastApiResponse,
                        onRefresh = viewModel::refreshPreview,
                        onSendTest = viewModel::sendTest,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroCard(uiState: UiState) {
    Card(
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.secondary,
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Kindroid Health Sync",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = "Mała aplikacja, która zbiera kontekst zdrowotny z telefonu i podaje go jednemu Kinowi w ludzkiej, polskiej formie.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroChip("Health Connect")
                    HeroChip("Auto sync ${uiState.settings.syncIntervalMinutes} min")
                    HeroChip("Jeden Kin")
                    HeroChip(if (uiState.hasHealthPermissions) "Dostęp gotowy" else "Czeka na zgody")
                }
                StatusStrip(
                    label = "Ostatni status",
                    value = uiState.status,
                    accent = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                    onAccent = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun HeroChip(text: String) {
    AssistChip(
        onClick = {},
        label = { Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
            labelColor = MaterialTheme.colorScheme.onPrimary,
            leadingIconContentColor = MaterialTheme.colorScheme.onPrimary,
            trailingIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f))
    )
}

@Composable
private fun HealthConnectCard(
    availability: HealthConnectAvailability,
    message: String,
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit,
    onRefresh: () -> Unit,
    onOpenHealthConnect: () -> Unit,
) {
    AppSectionCard(title = "Połączenie z Health Connect", subtitle = message) {
        StatusPill(
            text = when (availability) {
                HealthConnectAvailability.Available -> if (hasPermissions) "Gotowy" else "Wymaga zgód"
                HealthConnectAvailability.NotInstalled -> "Brak aplikacji"
                HealthConnectAvailability.UpdateRequired -> "Wymaga aktualizacji"
                HealthConnectAvailability.NotSupported -> "Brak wsparcia"
            },
            emphasized = availability == HealthConnectAvailability.Available && hasPermissions,
        )
        Text(
            text = "Jeśli Nadaj dostęp tylko miga, otwórz Health Connect ręcznie i nadaj dostęp tej aplikacji.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionButton(label = "Sprawdź", onClick = onRefresh)
            if (availability == HealthConnectAvailability.Available && !hasPermissions) {
                ActionButton(label = "Nadaj dostęp", onClick = onRequestPermissions, strong = true)
            }
        }
        ActionButton(label = "Otwórz Health Connect", onClick = onOpenHealthConnect)
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
    AppSectionCard(
        title = "Ustawienia synchronizacji",
        subtitle = "Klucz, identyfikator Kina i zakres danych, które mają budować wiadomość."
    ) {
        OutlinedTextField(
            value = state.apiKey,
            onValueChange = onApiKeyChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Kindroid API key") },
            placeholder = { Text("kn_...") },
            singleLine = true
        )
        OutlinedTextField(
            value = state.aiId,
            onValueChange = onAiIdChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Kindroid ai_id") },
            singleLine = true
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Interwał automatycznej synchronizacji", style = MaterialTheme.typography.titleMedium)
            IntervalChipRow(selectedValue = state.syncIntervalMinutes, onSelected = onIntervalChange)
        }
        OutlinedTextField(
            value = state.quietHours,
            onValueChange = onQuietHoursChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Godziny ciszy") },
            placeholder = { Text("23:00-07:00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ToggleRow("Sen", "Podsumowanie ostatniej nocy", state.includeSleep, onIncludeSleepChange)
        ToggleRow("Aktywność", "Dzisiejsze kroki i ruch", state.includeActivity, onIncludeActivityChange)
        ToggleRow("Tętno teraz", "Najnowszy pomiar", state.includeCurrentHeartRate, onIncludeCurrentHeartRateChange)
        ToggleRow("Średnie tętno", "Średnia z ostatnich 15 minut", state.includeAverageHeartRate, onIncludeAverageHeartRateChange)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntervalChipRow(
    selectedValue: Int,
    onSelected: (Int) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(15, 30, 60).forEach { minutes ->
            val selected = selectedValue == minutes
            AssistChip(
                onClick = { onSelected(minutes) },
                label = { Text("$minutes min") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    labelColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                ),
                border = BorderStroke(
                    1.dp,
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PreviewCard(
    context: Context,
    previewMessage: String,
    status: String,
    lastSyncAt: String,
    isLoading: Boolean,
    errorDetails: String,
    lastApiResponse: String,
    onRefresh: () -> Unit,
    onSendTest: () -> Unit
) {
    var showResponseDialog by remember { mutableStateOf(false) }

    if (showResponseDialog && lastApiResponse.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { showResponseDialog = false },
            confirmButton = {
                Button(onClick = { showResponseDialog = false }) {
                    Text("Zamknij")
                }
            },
            title = { Text("Ostatnia odpowiedź API") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(lastApiResponse, style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }

    AppSectionCard(
        title = "Wiadomość i status",
        subtitle = "Podgląd tego, co wyląduje u Kina, plus ręczne wywołanie testowe."
    ) {
        StatusStrip(
            label = "Ostatnia synchronizacja",
            value = lastSyncAt,
            accent = MaterialTheme.colorScheme.secondaryContainer,
            onAccent = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        StatusStrip(
            label = "Status",
            value = status,
            accent = MaterialTheme.colorScheme.tertiaryContainer,
            onAccent = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
        ) {
            Text(
                text = if (previewMessage.isBlank()) "Brak podglądu wiadomości. Najpierw odśwież dane z telefonu i sprawdź uprawnienia." else previewMessage,
                modifier = Modifier.padding(18.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionButton(label = "Odśwież podgląd", onClick = onRefresh)
            ActionButton(label = "Wyślij test", onClick = onSendTest, strong = true)
        }
        if (lastApiResponse.isNotBlank()) {
            ActionButton(label = "Otwórz odpowiedź", onClick = { showResponseDialog = true })
        }
        if (errorDetails.isNotBlank()) {
            ActionButton(
                label = "Kopiuj błąd",
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("kindroid-health-error", errorDetails))
                }
            )
        }
        if (isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.height(22.dp))
                Text("Trwa odświeżanie albo wysyłka.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun AppSectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

@Composable
private fun StatusStrip(
    label: String,
    value: String,
    accent: Color,
    onAccent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent, RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = onAccent.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.titleMedium, color = onAccent)
        }
        Box(
            modifier = Modifier
                .background(onAccent.copy(alpha = 0.12f), CircleShape)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text("live", style = MaterialTheme.typography.bodySmall, color = onAccent)
        }
    }
}

@Composable
private fun StatusPill(text: String, emphasized: Boolean) {
    Box(
        modifier = Modifier
            .background(
                if (emphasized) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit, strong: Boolean = false) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        colors = if (strong) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    ) {
        Text(label)
    }
}
