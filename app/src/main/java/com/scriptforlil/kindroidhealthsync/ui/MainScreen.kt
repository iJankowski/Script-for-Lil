package com.scriptforlil.kindroidhealthsync.ui

import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.scriptforlil.kindroidhealthsync.R
import com.scriptforlil.kindroidhealthsync.data.local.SyncHistoryEntry
import com.scriptforlil.kindroidhealthsync.data.local.SyncHistoryType
import com.scriptforlil.kindroidhealthsync.domain.MessageComposer
import com.scriptforlil.kindroidhealthsync.healthconnect.HealthConnectAvailability
import com.scriptforlil.kindroidhealthsync.healthconnect.HealthConnectNavigation

private enum class MainTab {
    Kin,
    Settings,
}

private enum class EditableSetting {
    KinName,
    ApiKey,
    AiId,
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    darkThemeEnabled: Boolean,
    heroExpanded: Boolean,
    onHeroExpandedChange: (Boolean) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Kin) }
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = viewModel.permissionContract()
    ) { granted ->
        viewModel.onPermissionsGranted(granted)
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Scaffold(
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
                .navigationBarsPadding(),
            containerColor = Color.Transparent,
            bottomBar = {
                BottomNavBar(
                    selectedTab = selectedTab,
                    darkThemeEnabled = darkThemeEnabled,
                    onDarkThemeChange = onDarkThemeChange,
                    onTabSelected = { selectedTab = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
                        HeroCard(
                            uiState = uiState,
                            expanded = heroExpanded,
                            onToggleExpanded = { onHeroExpandedChange(!heroExpanded) }
                        )
                        when (selectedTab) {
                            MainTab.Kin -> {
                                KinSpaceCard(
                                    kinName = uiState.settings.kinName,
                                    lastResponse = uiState.lastApiResponse,
                                    onEdit = { selectedTab = MainTab.Settings }
                                )
                                PreviewCard(
                                    context = context,
                                    previewMessage = uiState.previewMessage,
                                    status = uiState.status,
                                    lastSyncAt = uiState.lastSyncAt,
                                    autoSyncAt = uiState.autoSyncAt,
                                    autoSyncStatus = uiState.autoSyncStatus,
                                    workerState = uiState.workerState,
                                    syncIntervalMinutes = uiState.settings.syncIntervalMinutes,
                                    isLoading = uiState.isLoading,
                                    errorDetails = uiState.errorDetails,
                                    lastApiResponse = uiState.lastApiResponse,
                                    syncHistory = uiState.syncHistory,
                                    onRefresh = viewModel::refreshPreview,
                                    onSendTest = viewModel::sendTest,
                                )
                            }
                            MainTab.Settings -> {
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
                                    onKinNameChange = { value -> viewModel.updateSettings { it.copy(kinName = value) } },
                                    onApiKeyChange = { value -> viewModel.updateSettings { it.copy(apiKey = value) } },
                                    onAiIdChange = { value -> viewModel.updateSettings { it.copy(aiId = value) } },
                                    onMessageTemplateChange = { value -> viewModel.updateSettings { it.copy(messageTemplate = value) } },
                                    onMessageLanguageChange = { value -> viewModel.updateSettings { it.copy(messageLanguage = value) } },
                                    onLanguageChange = { value -> viewModel.updateSettings { it.copy(appLanguage = value) } },
                                    onQuietHoursChange = { value -> viewModel.updateSettings { it.copy(quietHours = value) } },
                                    onIntervalChange = viewModel::updateSyncInterval,
                                    onIncludeSleepChange = { value -> viewModel.updateSettings { it.copy(includeSleep = value) } },
                                    onIncludeActivityChange = { value -> viewModel.updateSettings { it.copy(includeActivity = value) } },
                                    onIncludeCurrentHeartRateChange = { value -> viewModel.updateSettings { it.copy(includeCurrentHeartRate = value) } },
                                    onIncludeAverageHeartRateChange = { value -> viewModel.updateSettings { it.copy(includeAverageHeartRate = value) } },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    selectedTab: MainTab,
    darkThemeEnabled: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onTabSelected: (MainTab) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarSlot(onClick = { onDarkThemeChange(!darkThemeEnabled) }) {
                ThemeModeIcon(darkThemeEnabled = darkThemeEnabled)
            }
            BottomBarSlot(onClick = { onTabSelected(MainTab.Kin) }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    KinNavIcon(selected = selectedTab == MainTab.Kin)
                    Text(
                        text = stringResource(R.string.nav_kin),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedTab == MainTab.Kin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            BottomBarSlot(onClick = { onTabSelected(MainTab.Settings) }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SettingsNavIcon(selected = selectedTab == MainTab.Settings)
                    Text(
                        text = stringResource(R.string.nav_settings),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedTab == MainTab.Settings) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
@Composable
private fun RowScope.BottomBarSlot(
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content,
    )
}

@Composable
private fun ThemeModeIcon(darkThemeEnabled: Boolean) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                if (darkThemeEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.secondaryContainer,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (darkThemeEnabled) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .align(Alignment.CenterEnd)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Box(modifier = Modifier.size(width = 22.dp, height = 22.dp)) {
                Box(
                    modifier = Modifier
                        .size(width = 2.dp, height = 6.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(999.dp))
                        .align(Alignment.TopCenter)
                )
                Box(
                    modifier = Modifier
                        .size(width = 2.dp, height = 6.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(999.dp))
                        .align(Alignment.BottomCenter)
                )
                Box(
                    modifier = Modifier
                        .size(width = 6.dp, height = 2.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(999.dp))
                        .align(Alignment.CenterStart)
                )
                Box(
                    modifier = Modifier
                        .size(width = 6.dp, height = 2.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(999.dp))
                        .align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
private fun KinNavIcon(selected: Boolean) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
    Box(modifier = Modifier.size(width = 28.dp, height = 20.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
                .align(Alignment.TopStart)
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
                .align(Alignment.TopEnd)
        )
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 8.dp)
                .background(color.copy(alpha = 0.85f), RoundedCornerShape(999.dp))
                .align(Alignment.BottomStart)
        )
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 8.dp)
                .background(color.copy(alpha = 0.85f), RoundedCornerShape(999.dp))
                .align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun SettingsNavIcon(selected: Boolean) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
    Column(
        modifier = Modifier.size(width = 24.dp, height = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(color, RoundedCornerShape(999.dp))
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroCard(
    uiState: UiState,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
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
                .clickable(onClick = onToggleExpanded)
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = if (expanded) Alignment.Start else Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.hero_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
                if (expanded) {
                    Text(
                        text = stringResource(R.string.hero_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f),
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeroChip(stringResource(R.string.hero_chip_health_connect))
                        HeroChip(stringResource(R.string.hero_chip_auto_sync, uiState.settings.syncIntervalMinutes))
                        HeroChip(stringResource(R.string.hero_chip_one_kin))
                        HeroChip(stringResource(if (uiState.hasHealthPermissions) R.string.hero_chip_access_ready else R.string.hero_chip_waiting_permissions))
                    }
                }
                StatusStrip(
                    label = stringResource(R.string.status_label_last),
                    value = uiState.status,
                    accent = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                    onAccent = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun KinSpaceCard(
    kinName: String,
    lastResponse: String,
    onEdit: () -> Unit,
) {
    AppSectionCard(
        title = stringResource(R.string.kin_placeholder_title),
        subtitle = stringResource(R.string.kin_space_subtitle)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KinNavIcon(selected = true)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = kinName.ifBlank { stringResource(R.string.kin_name_missing) },
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.kin_space_name_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            ActionButton(label = stringResource(R.string.button_edit), onClick = onEdit)
        }
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.kin_last_response_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = lastResponse.ifBlank { stringResource(R.string.kin_last_response_empty) },
                    style = MaterialTheme.typography.bodyLarge
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
    AppSectionCard(title = stringResource(R.string.section_health_connect_title), subtitle = message) {
        StatusPill(
            text = when (availability) {
                HealthConnectAvailability.Available -> stringResource(if (hasPermissions) R.string.health_connect_status_ready else R.string.health_connect_status_permissions)
                HealthConnectAvailability.NotInstalled -> stringResource(R.string.health_connect_status_missing_app)
                HealthConnectAvailability.UpdateRequired -> stringResource(R.string.health_connect_status_update_required)
                HealthConnectAvailability.NotSupported -> stringResource(R.string.health_connect_status_not_supported)
            },
            emphasized = availability == HealthConnectAvailability.Available && hasPermissions,
        )
        Text(
            text = stringResource(R.string.health_connect_hint_manual),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionButton(label = stringResource(R.string.button_check), onClick = onRefresh)
            if (availability == HealthConnectAvailability.Available && !hasPermissions) {
                ActionButton(label = stringResource(R.string.button_grant_access), onClick = onRequestPermissions, strong = true)
            }
        }
        ActionButton(label = stringResource(R.string.button_open_health_connect), onClick = onOpenHealthConnect)
    }
}

@Composable
private fun SettingsCard(
    state: SettingsState,
    onKinNameChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onAiIdChange: (String) -> Unit,
    onMessageTemplateChange: (String) -> Unit,
    onMessageLanguageChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onQuietHoursChange: (String) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onIncludeSleepChange: (Boolean) -> Unit,
    onIncludeActivityChange: (Boolean) -> Unit,
    onIncludeCurrentHeartRateChange: (Boolean) -> Unit,
    onIncludeAverageHeartRateChange: (Boolean) -> Unit,
) {
    val defaultTemplate = stringResource(R.string.message_template_default)
    var editedSetting by remember { mutableStateOf<EditableSetting?>(null) }
    var pendingValue by remember { mutableStateOf("") }
    var templateDialogOpen by remember { mutableStateOf(false) }
    var pendingTemplate by remember { mutableStateOf("") }

    if (editedSetting != null) {
        val titleRes = when (editedSetting) {
            EditableSetting.KinName -> R.string.dialog_edit_kin_name_title
            EditableSetting.ApiKey -> R.string.dialog_edit_kindroid_api_key_title
            EditableSetting.AiId -> R.string.dialog_edit_kindroid_ai_id_title
            null -> R.string.section_settings_title
        }
        AlertDialog(
            onDismissRequest = {
                editedSetting = null
                pendingValue = ""
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newValue = pendingValue.trim()
                        when (editedSetting) {
                            EditableSetting.KinName -> onKinNameChange(newValue)
                            EditableSetting.ApiKey -> onApiKeyChange(newValue)
                            EditableSetting.AiId -> onAiIdChange(newValue)
                            null -> Unit
                        }
                        editedSetting = null
                        pendingValue = ""
                    }
                ) {
                    Text(stringResource(R.string.button_save))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        editedSetting = null
                        pendingValue = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
            title = { Text(stringResource(titleRes)) },
            text = {
                OutlinedTextField(
                    value = pendingValue,
                    onValueChange = { pendingValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.dialog_edit_placeholder)) },
                    singleLine = true
                )
            }
        )
    }

    if (templateDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                templateDialogOpen = false
                pendingTemplate = ""
            },
            confirmButton = {
                Button(
                    onClick = {
                        onMessageTemplateChange(pendingTemplate)
                        templateDialogOpen = false
                        pendingTemplate = ""
                    }
                ) {
                    Text(stringResource(R.string.button_save))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        templateDialogOpen = false
                        pendingTemplate = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
            title = { Text(stringResource(R.string.dialog_edit_message_template_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = pendingTemplate,
                        onValueChange = { pendingTemplate = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text(stringResource(R.string.message_template_placeholder)) },
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.label_message_language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MessageLanguageChipRow(
                            selectedValue = state.messageLanguage,
                            onSelected = onMessageLanguageChange
                        )
                    }
                    TemplateTokenRow(
                        onInsert = { token ->
                            pendingTemplate = if (pendingTemplate.isBlank()) token else pendingTemplate.trimEnd() + "\n" + token
                        }
                    )
                }
            }
        )
    }

    AppSectionCard(
        title = stringResource(R.string.section_settings_title),
        subtitle = stringResource(R.string.section_settings_subtitle)
    ) {
        EditableSettingRow(
            label = stringResource(R.string.label_kin_name),
            value = state.kinName.ifBlank { stringResource(R.string.value_not_set) },
            onEdit = {
                editedSetting = EditableSetting.KinName
                pendingValue = ""
            }
        )
        EditableSettingRow(
            label = stringResource(R.string.label_kindroid_api_key),
            value = state.apiKey.ifBlank { stringResource(R.string.value_not_set) },
            onEdit = {
                editedSetting = EditableSetting.ApiKey
                pendingValue = ""
            }
        )
        EditableSettingRow(
            label = stringResource(R.string.label_kindroid_ai_id),
            value = state.aiId.ifBlank { stringResource(R.string.value_not_set) },
            onEdit = {
                editedSetting = EditableSetting.AiId
                pendingValue = ""
            }
        )
        EditableSettingRow(
            label = stringResource(R.string.label_message_template),
            value = state.messageTemplate.ifBlank { defaultTemplate },
            valueMaxLines = 3,
            onEdit = {
                templateDialogOpen = true
                pendingTemplate = state.messageTemplate.ifBlank { defaultTemplate }
            }
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.label_language), style = MaterialTheme.typography.titleMedium)
            LanguageChipRow(selectedValue = state.appLanguage, onSelected = onLanguageChange)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.label_sync_interval), style = MaterialTheme.typography.titleMedium)
            IntervalChipRow(selectedValue = state.syncIntervalMinutes, onSelected = onIntervalChange)
        }
        QuietHoursRow(
            value = state.quietHours,
            onValueChange = onQuietHoursChange,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ToggleRow(stringResource(R.string.toggle_sleep_title), stringResource(R.string.toggle_sleep_subtitle), state.includeSleep, onIncludeSleepChange)
        ToggleRow(stringResource(R.string.toggle_activity_title), stringResource(R.string.toggle_activity_subtitle), state.includeActivity, onIncludeActivityChange)
        ToggleRow(stringResource(R.string.toggle_hr_current_title), stringResource(R.string.toggle_hr_current_subtitle), state.includeCurrentHeartRate, onIncludeCurrentHeartRateChange)
        ToggleRow(stringResource(R.string.toggle_hr_avg_title), stringResource(R.string.toggle_hr_avg_subtitle), state.includeAverageHeartRate, onIncludeAverageHeartRateChange)
    }
}


@Composable
private fun QuietHoursRow(
    value: String,
    onValueChange: (String) -> Unit,
) {
    val context = LocalContext.current
    val (startHour, startMinute, endHour, endMinute) = remember(value) { parseQuietHours(value) }

    EditableSettingRow(
        label = stringResource(R.string.label_quiet_hours),
        value = value.ifBlank { stringResource(R.string.hint_quiet_hours) },
        onEdit = {
            TimePickerDialog(
                context,
                { _, pickedStartHour, pickedStartMinute ->
                    TimePickerDialog(
                        context,
                        { _, pickedEndHour, pickedEndMinute ->
                            onValueChange(
                                formatQuietHours(
                                    pickedStartHour,
                                    pickedStartMinute,
                                    pickedEndHour,
                                    pickedEndMinute,
                                )
                            )
                        },
                        endHour,
                        endMinute,
                        true,
                    ).show()
                },
                startHour,
                startMinute,
                true,
            ).show()
        }
    )
}

private fun parseQuietHours(value: String): QuietHoursParts {
    val parts = value.split("-")
    val start = parts.getOrNull(0)?.split(":") ?: emptyList()
    val end = parts.getOrNull(1)?.split(":") ?: emptyList()

    return QuietHoursParts(
        startHour = start.getOrNull(0)?.toIntOrNull() ?: 23,
        startMinute = start.getOrNull(1)?.toIntOrNull() ?: 0,
        endHour = end.getOrNull(0)?.toIntOrNull() ?: 7,
        endMinute = end.getOrNull(1)?.toIntOrNull() ?: 0,
    )
}

private fun formatQuietHours(
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
): String {
    return "%02d:%02d-%02d:%02d".format(startHour, startMinute, endHour, endMinute)
}

private data class QuietHoursParts(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
)
@Composable
private fun EditableSettingRow(
    label: String,
    value: String,
    valueMaxLines: Int = 1,
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f), RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                maxLines = valueMaxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        ActionButton(label = stringResource(R.string.button_edit), onClick = onEdit)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TemplateTokenRow(
    onInsert: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(
            MessageComposer.TEMPLATE_SLEEP to stringResource(R.string.template_token_sleep),
            MessageComposer.TEMPLATE_ACTIVITY to stringResource(R.string.template_token_activity),
            MessageComposer.TEMPLATE_HEART_RATE_NOW to stringResource(R.string.template_token_heart_rate_now),
            MessageComposer.TEMPLATE_HEART_RATE_AVG to stringResource(R.string.template_token_heart_rate_avg),
        ).forEach { (token, label) ->
            AssistChip(
                onClick = { onInsert(token) },
                label = { Text(label) },
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MessageLanguageChipRow(
    selectedValue: String,
    onSelected: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            "pl" to stringResource(R.string.language_polish),
            "en" to stringResource(R.string.language_english),
        ).forEach { (value, label) ->
            val selected = selectedValue == value
            AssistChip(
                onClick = { onSelected(value) },
                label = { Text(label) },
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
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LanguageChipRow(
    selectedValue: String,
    onSelected: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(
            "system" to stringResource(R.string.language_system),
            "pl" to stringResource(R.string.language_polish),
            "en" to stringResource(R.string.language_english),
        ).forEach { (value, label) ->
            val selected = selectedValue == value
            AssistChip(
                onClick = { onSelected(value) },
                label = { Text(label) },
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
                label = { Text(stringResource(R.string.interval_minutes, minutes)) },
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
    autoSyncAt: String,
    autoSyncStatus: String,
    workerState: String,
    syncIntervalMinutes: Int,
    isLoading: Boolean,
    errorDetails: String,
    lastApiResponse: String,
    syncHistory: List<SyncHistoryEntry>,
    onRefresh: () -> Unit,
    onSendTest: () -> Unit
) {
    var showResponseDialog by remember { mutableStateOf(false) }
    var showSyncHistoryDialog by remember { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf(false) }


    if (showSyncHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showSyncHistoryDialog = false },
            confirmButton = {
                Button(onClick = { showSyncHistoryDialog = false }) {
                    Text(stringResource(R.string.button_close))
                }
            },
            title = { Text(stringResource(R.string.dialog_sync_history_title)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (syncHistory.isEmpty()) {
                        Text(
                            text = stringResource(R.string.sync_history_empty),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        syncHistory.forEach { entry ->
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(entry.at, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = if (entry.type == SyncHistoryType.AUTO) stringResource(R.string.sync_type_auto) else stringResource(R.string.sync_type_manual),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        entry.status,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
    }
    if (showResponseDialog && lastApiResponse.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { showResponseDialog = false },
            confirmButton = {
                Button(onClick = { showResponseDialog = false }) {
                    Text(stringResource(R.string.button_close))
                }
            },
            title = { Text(stringResource(R.string.dialog_api_response_title)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(lastApiResponse, style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.section_preview_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.section_preview_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (expanded) "▴" else "▾",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            if (expanded) {
                StatusStrip(
                    label = stringResource(R.string.status_label_last_sync),
                    value = lastSyncAt,
                    accent = MaterialTheme.colorScheme.secondaryContainer,
                    onAccent = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                StatusStrip(
                    label = stringResource(R.string.status_label_auto_sync),
                    value = autoSyncAt,
                    accent = MaterialTheme.colorScheme.primaryContainer,
                    onAccent = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                StatusStrip(
                    label = stringResource(R.string.status_label_auto_sync_status),
                    value = autoSyncStatus,
                    accent = MaterialTheme.colorScheme.surfaceVariant,
                    onAccent = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatusStrip(
                    label = stringResource(R.string.status_label_worker_state),
                    value = workerState,
                    accent = MaterialTheme.colorScheme.secondaryContainer,
                    onAccent = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                StatusStrip(
                    label = stringResource(R.string.status_label_status),
                    value = status,
                    accent = MaterialTheme.colorScheme.tertiaryContainer,
                    onAccent = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                ) {
                    Text(
                        text = if (previewMessage.isBlank()) stringResource(R.string.preview_empty) else previewMessage,
                        modifier = Modifier.padding(18.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionButton(label = stringResource(R.string.button_refresh_preview), onClick = onRefresh)
                    ActionButton(label = stringResource(R.string.button_send_test), onClick = onSendTest, strong = true)
                }
                ActionButton(label = stringResource(R.string.button_open_sync_history), onClick = { showSyncHistoryDialog = true })
                if (lastApiResponse.isNotBlank()) {
                    ActionButton(label = stringResource(R.string.button_open_response), onClick = { showResponseDialog = true })
                }
                if (errorDetails.isNotBlank()) {
                    ActionButton(
                        label = stringResource(R.string.button_copy_error),
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("kindroid-health-error", errorDetails))
                        }
                    )
                }
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.height(22.dp))
                        Text(stringResource(R.string.loading_message), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                val timestampFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }
                val lastManual = remember(lastSyncAt) {
                    runCatching { LocalDateTime.parse(lastSyncAt, timestampFormatter) }.getOrNull()
                }
                val lastAuto = remember(autoSyncAt) {
                    runCatching { LocalDateTime.parse(autoSyncAt, timestampFormatter) }.getOrNull()
                }
                val latestSync = when {
                    lastManual == null -> lastAuto
                    lastAuto == null -> lastManual
                    lastAuto.isAfter(lastManual) -> lastAuto
                    else -> lastManual
                }
                val latestSyncLabel = when (latestSync) {
                    null -> stringResource(R.string.status_label_last)
                    lastAuto -> stringResource(R.string.status_label_auto_sync)
                    else -> stringResource(R.string.status_label_last_sync)
                }
                val latestSyncValue = latestSync?.format(timestampFormatter) ?: stringResource(R.string.status_none)
                val minutesUntilNext = latestSync?.let {
                    val elapsed = Duration.between(it, LocalDateTime.now()).toMinutes().coerceAtLeast(0)
                    (syncIntervalMinutes - elapsed).coerceAtLeast(0)
                }
                val nextSyncValue = when {
                    latestSync == null -> stringResource(R.string.next_sync_now)
                    minutesUntilNext == null || minutesUntilNext <= 0 -> stringResource(R.string.next_sync_now)
                    else -> stringResource(R.string.next_sync_in_minutes, minutesUntilNext)
                }

                StatusStrip(
                    label = latestSyncLabel,
                    value = latestSyncValue,
                    accent = MaterialTheme.colorScheme.primaryContainer,
                    onAccent = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                StatusStrip(
                    label = stringResource(R.string.status_label_next_sync),
                    value = nextSyncValue,
                    accent = MaterialTheme.colorScheme.secondaryContainer,
                    onAccent = MaterialTheme.colorScheme.onSecondaryContainer,
                )
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
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent, RoundedCornerShape(22.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
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
            Text(stringResource(R.string.status_live), style = MaterialTheme.typography.bodySmall, color = onAccent)
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

















































