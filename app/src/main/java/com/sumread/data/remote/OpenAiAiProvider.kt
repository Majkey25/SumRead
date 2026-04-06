package com.sumread.data.remote

import com.sumread.domain.model.AiProviderType
import com.sumread.domain.model.ChatMessage
import com.sumread.domain.model.ChatRole
import com.sumread.domain.model.OperationException
import com.sumread.domain.model.OperationFailure
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

@Singleton
class OpenAiAiProvider @Inject constructor(
    private val openAiApiService: OpenAiApiService,
) : AiProvider {

    override val type: AiProviderType = AiProviderType.OPENAI

    override suspend fun summarize(apiKey: String, model: String, sourceText: String): Result<String> {
        return runCatching {
            val response = openAiApiService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = OpenAiChatRequest(
                    model = model,
                    temperature = 0.2f,
                    messages = listOf(
                        OpenAiMessage(role = "system", content = AiPromptFactory.summarySystemPrompt()),
                        OpenAiMessage(role = "user", content = AiPromptFactory.summaryUserPrompt(sourceText)),
                    ),
                ),
            )
            response.choices.firstOrNull()?.message?.content?.trim()
                ?.takeIf(String::isNotBlank)
                ?: throw OperationException(OperationFailure.ProviderFailure("OpenAI returned an empty response."))
        }.mapFailure()
    }

    override suspend fun reply(
        apiKey: String,
        model: String,
        contextText: String,
        conversation: List<ChatMessage>,
        userMessage: String,
    ): Result<String> {
        return runCatching {
            val response = openAiApiService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = OpenAiChatRequest(
                    model = model,
                    temperature = 0.3f,
                    messages = AiPromptFactory.chatMessages(contextText, conversation, userMessage).map {
                        OpenAiMessage(role = it.role, content = it.content)
                    },
                ),
            )
            response.choices.firstOrNull()?.message?.content?.trim()
                ?.takeIf(String::isNotBlank)
                ?: throw OperationException(OperationFailure.ProviderFailure("OpenAI returned an empty response."))
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
                        OperationFailure.ProviderFailure("OpenAI request failed with HTTP ${error.code()}.")
                    )

                    is OperationException -> error
                    else -> OperationException(
                        OperationFailure.ProviderFailure(error.message ?: "OpenAI request failed."),
                    )
                },
            )
        },
    )
}