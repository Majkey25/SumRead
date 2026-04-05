package com.sumread.data.repository

import com.sumread.data.remote.AiProvider
import com.sumread.data.remote.GeminiAiProvider
import com.sumread.data.remote.GroqAiProvider
import com.sumread.domain.model.AiProviderType
import com.sumread.domain.model.ChatMessage
import com.sumread.domain.model.OperationFailure
import com.sumread.domain.model.failureResult
import com.sumread.domain.repository.AiRepository
import com.sumread.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val groqAiProvider: GroqAiProvider,
    private val geminiAiProvider: GeminiAiProvider,
) : AiRepository {

    override suspend fun summarize(sourceText: String): Result<String> {
        val providerWithKey = selectedProvider() ?: return failureResult(OperationFailure.MissingApiKey)
        return providerWithKey.first.summarize(
            apiKey = providerWithKey.second,
            sourceText = sourceText,
        )
    }

    override suspend fun reply(
        contextText: String,
        conversation: List<ChatMessage>,
        userMessage: String,
    ): Result<String> {
        val providerWithKey = selectedProvider() ?: return failureResult(OperationFailure.MissingApiKey)
        return providerWithKey.first.reply(
            apiKey = providerWithKey.second,
            contextText = contextText,
            conversation = conversation,
            userMessage = userMessage,
        )
    }

    private suspend fun selectedProvider(): Pair<AiProvider, String>? {
        val settings = settingsRepository.settings.first()
        val apiKey = settingsRepository.getApiKey(settings.selectedProvider)?.trim().orEmpty()
        if (apiKey.isBlank()) {
            return null
        }
        return when (settings.selectedProvider) {
            AiProviderType.GROQ -> groqAiProvider to apiKey
            AiProviderType.GEMINI -> geminiAiProvider to apiKey
        }
    }
}
