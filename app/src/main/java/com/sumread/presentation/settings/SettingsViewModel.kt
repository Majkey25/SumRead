package com.sumread.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumread.domain.model.AiProviderType
import com.sumread.domain.model.AppSettings
import com.sumread.domain.model.PermissionSnapshot
import com.sumread.domain.repository.OverlayController
import com.sumread.domain.repository.SettingsRepository
import com.sumread.domain.usecase.ObserveSettingsUseCase
import com.sumread.domain.usecase.SaveProviderApiKeyUseCase
import com.sumread.domain.usecase.StartOverlayUseCase
import com.sumread.domain.usecase.StopOverlayUseCase
import com.sumread.domain.usecase.UpdateSettingsUseCase
import com.sumread.presentation.common.PermissionStatusResolver
import com.sumread.util.AppConfig
import com.sumread.util.LanguageCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettingsUseCase: ObserveSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val saveProviderApiKeyUseCase: SaveProviderApiKeyUseCase,
    private val startOverlayUseCase: StartOverlayUseCase,
    private val stopOverlayUseCase: StopOverlayUseCase,
    private val settingsRepository: SettingsRepository,
    overlayController: OverlayController,
    private val permissionStatusResolver: PermissionStatusResolver,
) : ViewModel() {

    private val permissionState = MutableStateFlow(permissionStatusResolver.snapshot())
    private val messageState = MutableStateFlow<String?>(null)

    val uiState = observeSettingsUseCase()
        .combine(overlayController.isRunning) { settings, isRunning ->
            settings to isRunning
        }
        .combine(permissionState) { pair, permissions ->
            Triple(pair.first, pair.second, permissions)
        }
        .combine(messageState) { triple, message ->
            Quadruple(triple.first, triple.second, triple.third, message)
        }
        .mapLatest { (settings, isRunning, permissions, message) ->
            SettingsUiState(
                settings = settings,
                permissions = permissions,
                isOverlayRunning = isRunning,
                groqConfigured = settingsRepository.hasApiKey(AiProviderType.GROQ),
                geminiConfigured = settingsRepository.hasApiKey(AiProviderType.GEMINI),
                transientMessage = message,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = SettingsUiState(
                settings = AppSettings(
                    selectedProvider = AiProviderType.GROQ,
                    speechRate = AppConfig.defaultSpeechRate,
                    speechPitch = AppConfig.defaultSpeechPitch,
                    languageTag = LanguageCatalog.supportedOptions.first().languageTag,
                ),
                permissions = PermissionSnapshot(
                    overlayGranted = false,
                    microphoneGranted = false,
                ),
                isOverlayRunning = false,
                groqConfigured = false,
                geminiConfigured = false,
                transientMessage = null,
            ),
        )

    fun refreshPermissions() {
        permissionState.value = permissionStatusResolver.snapshot()
    }

    fun updateProvider(provider: AiProviderType) {
        viewModelScope.launch {
            updateSettingsUseCase.provider(provider)
        }
    }

    fun updateSpeechRate(value: Float) {
        viewModelScope.launch {
            updateSettingsUseCase.speechRate(value)
        }
    }

    fun updateSpeechPitch(value: Float) {
        viewModelScope.launch {
            updateSettingsUseCase.speechPitch(value)
        }
    }

    fun updateLanguageTag(value: String) {
        viewModelScope.launch {
            updateSettingsUseCase.languageTag(value)
        }
    }

    fun saveApiKey(provider: AiProviderType, value: String) {
        viewModelScope.launch {
            saveProviderApiKeyUseCase(provider, value)
            messageState.value = "${provider.title} API key saved."
        }
    }

    fun startOverlay() {
        startOverlayUseCase()
        messageState.value = "Overlay start requested."
    }

    fun stopOverlay() {
        stopOverlayUseCase()
        messageState.value = "Overlay stop requested."
    }

    fun clearMessage() {
        messageState.value = null
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)
