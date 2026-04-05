package com.sumread.data.remote

import com.sumread.domain.model.AiProviderType
import com.sumread.domain.model.ChatMessage
import com.sumread.domain.model.OperationFailure
import com.sumread.domain.model.OperationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

@Singleton
class GroqAiProvider @Inject constructor(
    private val groqApiService: GroqApiService,
) : AiProvider {

    override val type: AiProviderType = AiProviderType.GROQ

    override suspend fun summarize(apiKey: String, sourceText: String): Result<String> {
        return runCatching {
            val response = groqApiService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = GroqChatRequest(
                    model = com.sumread.util.AppConfig.groqModel,
                    temperature = 0.2f,
                    messages = listOf(
                        GroqMessage(role = "system", content = AiPromptFactory.summarySystemPrompt()),
                        GroqMessage(role = "user", content = AiPromptFactory.summaryUserPrompt(sourceText)),
                    ),
                ),
            )
            response.choices.firstOrNull()?.message?.content?.trim()
                ?.takeIf(String::isNotBlank)
                ?: throw OperationException(OperationFailure.ProviderFailure("Groq returned an empty response."))
        }.mapFailure()
    }

    override suspend fun reply(
        apiKey: String,
        contextText: String,
        conversation: List<ChatMessage>,
        userMessage: String,
    ): Result<String> {
        return runCatching {
            val response = groqApiService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = GroqChatRequest(
                    model = com.sumread.util.AppConfig.groqModel,
                    temperature = 0.3f,
                    messages = AiPromptFactory.chatMessages(contextText, conversation, userMessage)
                        .map { GroqMessage(role = it.role, content = it.content) },
                ),
            )
            response.choices.firstOrNull()?.message?.content?.trim()
                ?.takeIf(String::isNotBlank)
                ?: throw OperationException(OperationFailure.ProviderFailure("Groq returned an empty response."))
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
                        OperationFailure.ProviderFailure("Groq request failed with HTTP ${error.code()}."),
                    )

                    is OperationException -> error
                    else -> OperationException(
                        OperationFailure.ProviderFailure(error.message ?: "Groq request failed."),
                    )
                },
            )
        },
    )
}
