package com.sumread.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sumread.domain.model.AiProviderType
import com.sumread.presentation.common.SumReadTopBar
import com.sumread.util.AppConfig
import com.sumread.util.LanguageCatalog

@Composable
fun SettingsScreen(
    onOpenOverlaySettings: () -> Unit,
    onRequestMicrophonePermission: () -> Unit,
    onOpenTtsSettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.transientMessage) {
        val message = uiState.transientMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearMessage()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SumReadTopBar(
                    title = "SumRead",
                    subtitle = "Local OCR first, optional AI second. Every sensitive action is user initiated.",
                )
            }
            item { PrivacyCard() }
            item {
                PermissionCard(
                    overlayGranted = uiState.permissions.overlayGranted,
                    microphoneGranted = uiState.permissions.microphoneGranted,
                    onOpenOverlaySettings = onOpenOverlaySettings,
                    onRequestMicrophonePermission = onRequestMicrophonePermission,
                )
            }
            item {
                OverlayControlCard(
                    isOverlayRunning = uiState.isOverlayRunning,
                    canStart = uiState.permissions.overlayGranted,
                    onStart = viewModel::startOverlay,
                    onStop = viewModel::stopOverlay,
                )
            }
            item {
                ProviderCard(
                    selectedProvider = uiState.settings.selectedProvider,
                    groqConfigured = uiState.groqConfigured,
                    geminiConfigured = uiState.geminiConfigured,
                    openaiConfigured = uiState.openaiConfigured,
                    onProviderSelected = viewModel::updateProvider,
                    onSaveApiKey = viewModel::saveApiKey,
                    onClearApiKey = viewModel::clearApiKey,
                    selectedGroqModel = uiState.settings.groqModel,
                    selectedGeminiModel = uiState.settings.geminiModel,
                    selectedOpenaiModel = uiState.settings.openaiModel,
                    onModelSelected = viewModel::updateModel,
                )
            }
            item {
                SpeechCard(
                    speechRate = uiState.settings.speechRate,
                    speechPitch = uiState.settings.speechPitch,
                    languageTag = uiState.settings.languageTag,
                    onRateChanged = viewModel::updateSpeechRate,
                    onPitchChanged = viewModel::updateSpeechPitch,
                    onLanguageChanged = viewModel::updateLanguageTag,
                    onOpenTtsSettings = onOpenTtsSettings,
                )
            }
            item { InformationCard() }
            item { SnackbarHost(hostState = snackbarHostState) }
        }
    }
}

@Composable
private fun PrivacyCard() {
    SectionCard(title = "Privacy first") {
        Text(
            text = "Read aloud uses on-device OCR and on-device TTS by default. AI summary and chat stay disabled until you provide your own provider key.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun PermissionCard(
    overlayGranted: Boolean,
    microphoneGranted: Boolean,
    onOpenOverlaySettings: () -> Unit,
    onRequestMicrophonePermission: () -> Unit,
) {
    SectionCard(title = "Permissions") {
        PermissionRow(
            title = "Overlay",
            detail = if (overlayGranted) "Granted." else "Required to show the floating button above other apps.",
            actionLabel = if (overlayGranted) "Review" else "Grant",
            onAction = onOpenOverlaySettings,
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        PermissionRow(
            title = "Microphone",
            detail = if (microphoneGranted) "Granted." else "Optional for speech input in chat.",
            actionLabel = if (microphoneGranted) "Recheck" else "Grant",
            onAction = onRequestMicrophonePermission,
        )
    }
}

@Composable
private fun OverlayControlCard(
    isOverlayRunning: Boolean,
    canStart: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    SectionCard(title = "Overlay control") {
        Text(
            text = if (isOverlayRunning) {
                "The foreground overlay service is active."
            } else {
                "The overlay service is stopped."
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onStart,
                enabled = canStart && !isOverlayRunning,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "Start overlay")
            }
            TextButton(
                onClick = onStop,
                enabled = isOverlayRunning,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "Stop overlay")
            }
        }
        if (!canStart) {
            Text(
                text = "Grant overlay permission before starting the floating button.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProviderCard(
    selectedProvider: AiProviderType,
    groqConfigured: Boolean,
    geminiConfigured: Boolean,
    openaiConfigured: Boolean,
    selectedGroqModel: String,
    selectedGeminiModel: String,
    selectedOpenaiModel: String,
    onProviderSelected: (AiProviderType) -> Unit,
    onSaveApiKey: (AiProviderType, String) -> Unit,
    onClearApiKey: (AiProviderType) -> Unit,
    onModelSelected: (AiProviderType, String) -> Unit,
) {
    var groqKey by rememberSaveable { mutableStateOf("") }
    var geminiKey by rememberSaveable { mutableStateOf("") }
    var openaiKey by rememberSaveable { mutableStateOf("") }

    SectionCard(title = "AI providers") {
        Text(
            text = "Cloud AI is opt in. Only the OCR text from your selected region is sent to the provider you choose.",
            style = MaterialTheme.typography.bodyMedium,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 12.dp),
        ) {
            AiProviderType.entries.forEach { provider ->
                val configured = when (provider) {
                    AiProviderType.GROQ -> groqConfigured
                    AiProviderType.GEMINI -> geminiConfigured
                    AiProviderType.OPENAI -> openaiConfigured
                }
                AssistChip(
                    onClick = { onProviderSelected(provider) },
                    label = {
                        val suffix = if (provider == selectedProvider) " (active)" else if (configured) " configured" else ""
                        Text(text = provider.title + suffix)
                    },
                )
            }
        }
        val (activeModels, activeModel) = when (selectedProvider) {
            AiProviderType.GROQ -> AppConfig.groqModels to selectedGroqModel
            AiProviderType.GEMINI -> AppConfig.geminiModels to selectedGeminiModel
            AiProviderType.OPENAI -> AppConfig.openaiModels to selectedOpenaiModel
        }
        Text(
            text = "Model (${selectedProvider.title})",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 12.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            activeModels.forEach { modelId ->
                AssistChip(
                    onClick = { onModelSelected(selectedProvider, modelId) },
                    label = {
                        val suffix = if (modelId == activeModel) " selected" else ""
                        Text(text = modelId + suffix)
                    },
                )
            }
        }
        ApiKeyField(
            title = "Groq API key",
            value = groqKey,
            configured = groqConfigured,
            onValueChanged = { groqKey = it },
            onSave = { onSaveApiKey(AiProviderType.GROQ, groqKey) },
            onClear = { onClearApiKey(AiProviderType.GROQ) },
        )
        ApiKeyField(
            title = "Gemini API key",
            value = geminiKey,
            configured = geminiConfigured,
            onValueChanged = { geminiKey = it },
            onSave = { onSaveApiKey(AiProviderType.GEMINI, geminiKey) },
            onClear = { onClearApiKey(AiProviderType.GEMINI) },
        )
        ApiKeyField(
            title = "OpenAI API key",
            value = openaiKey,
            configured = openaiConfigured,
            onValueChanged = { openaiKey = it },
            onSave = { onSaveApiKey(AiProviderType.OPENAI, openaiKey) },
            onClear = { onClearApiKey(AiProviderType.OPENAI) },
        )
    }
}

@Composable
private fun ApiKeyField(
    title: String,
    value: String,
    configured: Boolean,
    onValueChanged: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (configured) "$title (saved securely)" else title,
            style = MaterialTheme.typography.titleSmall,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Password,
            ),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = onSave,
                enabled = value.isNotBlank(),
            ) {
                Text(text = "Save")
            }
            if (configured) {
                TextButton(onClick = onClear) {
                    Text(
                        text = "Clear",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpeechCard(
    speechRate: Float,
    speechPitch: Float,
    languageTag: String,
    onRateChanged: (Float) -> Unit,
    onPitchChanged: (Float) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onOpenTtsSettings: () -> Unit,
) {
    SectionCard(title = "Speech and language") {
        Text(
            text = "Uses the on-device TTS engine. Language packs must be installed via your device's TTS settings. OCR currently uses the on-device Latin recognizer.",
            style = MaterialTheme.typography.bodyMedium,
        )
        TextButton(
            onClick = onOpenTtsSettings,
            modifier = Modifier.padding(top = 4.dp),
        ) {
            Text(text = "Install language packs (opens TTS settings)")
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Language",
            style = MaterialTheme.typography.titleSmall,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LanguageCatalog.supportedOptions.forEach { option ->
                AssistChip(
                    onClick = { onLanguageChanged(option.languageTag) },
                    label = {
                        val suffix = if (option.languageTag == languageTag) " \u2713" else ""
                        Text(text = option.title + suffix)
                    },
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Speech rate: ${"%.2f".format(speechRate)}",
            style = MaterialTheme.typography.titleSmall,
        )
        Slider(
            value = speechRate,
            onValueChange = onRateChanged,
            valueRange = 0.6f..1.6f,
        )
        Text(
            text = "Speech pitch: ${"%.2f".format(speechPitch)}",
            style = MaterialTheme.typography.titleSmall,
        )
        Slider(
            value = speechPitch,
            onValueChange = onPitchChanged,
            valueRange = 0.7f..1.4f,
        )
    }
}

@Composable
private fun InformationCard() {
    SectionCard(title = "Capture flow") {
        Text(
            text = "Every screen capture is user initiated. Android will show the system MediaProjection consent screen for each session.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Captured text is not stored by default. Chat history stays in memory only and is cleared when the process ends or the user closes the chat session.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun PermissionRow(
    title: String,
    detail: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = detail, style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onAction) {
            Text(text = actionLabel)
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            content()
        }
    }
}
