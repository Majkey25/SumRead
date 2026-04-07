package com.sumread.data.repository

import com.sumread.data.remote.AiProvider
import com.sumread.data.remote.GeminiAiProvider
import com.sumread.data.remote.GroqAiProvider
import com.sumread.data.remote.OpenAiAiProvider
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
    private val openAiAiProvider: OpenAiAiProvider,
) : AiRepository {

    override suspend fun summarize(sourceText: String): Result<String> {
        val (provider, apiKey, model) = selectedProvider() ?: return failureResult(OperationFailure.MissingApiKey)
        return provider.summarize(apiKey = apiKey, model = model, sourceText = sourceText)
    }

    override suspend fun reply(
        contextText: String,
        conversation: List<ChatMessage>,
        userMessage: String,
    ): Result<String> {
        val (provider, apiKey, model) = selectedProvider() ?: return failureResult(OperationFailure.MissingApiKey)
        return provider.reply(
            apiKey = apiKey,
            model = model,
            contextText = contextText,
            conversation = conversation,
            userMessage = userMessage,
        )
    }

    private suspend fun selectedProvider(): Triple<AiProvider, String, String>? {
        val settings = settingsRepository.settings.first()
        val orderedTypes = buildList {
            add(settings.selectedProvider)
            AiProviderType.entries.forEach { if (it != settings.selectedProvider) add(it) }
        }
        for (type in orderedTypes) {
            val apiKey = settingsRepository.getApiKey(type)?.trim().orEmpty()
            if (apiKey.isNotBlank()) {
                return when (type) {
                    AiProviderType.GROQ -> Triple(groqAiProvider, apiKey, settings.groqModel)
                    AiProviderType.GEMINI -> Triple(geminiAiProvider, apiKey, settings.geminiModel)
                    AiProviderType.OPENAI -> Triple(openAiAiProvider, apiKey, settings.openaiModel)
                }
            }
        }
        return null
    }
}
