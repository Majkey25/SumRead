package com.sumread.data.remote

import com.sumread.domain.model.AiProviderType
import com.sumread.domain.model.ChatMessage
import com.sumread.domain.model.ChatRole
import com.sumread.domain.model.OperationFailure
import com.sumread.domain.model.OperationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

@Singleton
class GeminiAiProvider @Inject constructor(
    private val geminiApiService: GeminiApiService,
) : AiProvider {

    override val type: AiProviderType = AiProviderType.GEMINI

    override suspend fun summarize(apiKey: String, sourceText: String): Result<String> {
        return runCatching {
            val response = geminiApiService.generateContent(
                model = com.sumread.util.AppConfig.geminiModel,
                apiKey = apiKey,
                request = GeminiGenerateContentRequest(
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = AiPromptFactory.summarySystemPrompt())),
                    ),
                    contents = listOf(
                        GeminiContent(
                            role = "user",
                            parts = listOf(GeminiPart(text = AiPromptFactory.summaryUserPrompt(sourceText))),
                        ),
                    ),
                ),
            )
            response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.joinToString(separator = "\n") { it.text }
                ?.trim()
                ?.takeIf(String::isNotBlank)
                ?: throw OperationException(OperationFailure.ProviderFailure("Gemini returned an empty response."))
        }.mapFailure()
    }

    override suspend fun reply(
        apiKey: String,
        contextText: String,
        conversation: List<ChatMessage>,
        userMessage: String,
    ): Result<String> {
        return runCatching {
            val response = geminiApiService.generateContent(
                model = com.sumread.util.AppConfig.geminiModel,
                apiKey = apiKey,
                request = GeminiGenerateContentRequest(
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = AiPromptFactory.chatSystemPrompt(contextText))),
                    ),
                    contents = conversation.map { message ->
                        GeminiContent(
                            role = when (message.role) {
                                ChatRole.USER -> "user"
                                ChatRole.ASSISTANT -> "model"
                            },
                            parts = listOf(GeminiPart(text = message.text)),
                        )
                    } + GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = userMessage)),
                    ),
                ),
            )
            response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.joinToString(separator = "\n") { it.text }
                ?.trim()
                ?.takeIf(String::isNotBlank)
                ?: throw OperationException(OperationFailure.ProviderFailure("Gemini returned an empty response."))
        }.mapFailure()
    }
}

private fun <T> Result<T>.mapFailure(): Result<T> {
    return fold(
        onSuccess = Result.Companion::success,
        onFailure = { error ->
            Result.failure(
                when (error) {
                    is UnknownHostException,
                    is SocketTimeoutException,
                    is IOException,
                    -> OperationException(OperationFailure.NetworkUnavailable)

                    is HttpException -> OperationException(
                        OperationFailure.ProviderFailure("Gemini request failed with HTTP ${error.code()}."),
                    )

                    is OperationException -> error
                    else -> OperationException(
                        OperationFailure.ProviderFailure(error.message ?: "Gemini request failed."),
                    )
                },
            )
        },
    )
}
