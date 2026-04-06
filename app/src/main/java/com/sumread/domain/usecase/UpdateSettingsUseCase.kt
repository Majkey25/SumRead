package com.sumread.domain.usecase

import com.sumread.domain.model.AiProviderType
import com.sumread.domain.repository.SettingsRepository
import com.sumread.util.AppConfig
import javax.inject.Inject
import kotlinx.coroutines.flow.first


class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend fun provider(provider: AiProviderType) {
        val current = settingsRepository.settings.first()
        settingsRepository.updateSettings(current.copy(selectedProvider = provider))
    }

    suspend fun speechRate(value: Float) {
        val current = settingsRepository.settings.first()
        settingsRepository.updateSettings(
            current.copy(speechRate = value.coerceIn(AppConfig.minSpeechRate, AppConfig.maxSpeechRate)),
        )
    }

    suspend fun speechPitch(value: Float) {
        val current = settingsRepository.settings.first()
        settingsRepository.updateSettings(
            current.copy(speechPitch = value.coerceIn(AppConfig.minSpeechPitch, AppConfig.maxSpeechPitch)),
        )
    }

    suspend fun languageTag(value: String) {
        val current = settingsRepository.settings.first()
        settingsRepository.updateSettings(current.copy(languageTag = value))
    }

    suspend fun model(provider: AiProviderType, modelId: String) {
        val current = settingsRepository.settings.first()
        val updated = when (provider) {
            AiProviderType.GROQ -> current.copy(groqModel = modelId)
            AiProviderType.GEMINI -> current.copy(geminiModel = modelId)
            AiProviderType.OPENAI -> current.copy(openaiModel = modelId)
        }
        settingsRepository.updateSettings(updated)
    }
}
