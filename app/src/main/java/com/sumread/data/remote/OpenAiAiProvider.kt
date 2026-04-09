package com.sumread.data.remote

import com.sumread.domain.model.AiProviderType
import com.sumread.domain.model.ChatMessage
import com.sumread.domain.model.ErrorCode
import com.sumread.domain.model.OperationException
import com.sumread.domain.model.OperationFailure
import com.sumread.util.SecureLogger
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
        SecureLogger.secureApiCall("OpenAI", "summarize")
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
                ?: throw OperationException(OperationFailure.ProviderFailure(ErrorCode.PROVIDER_ERROR))
        }.mapFailure("summarize")
    }

    override suspend fun reply(
        apiKey: String,
        model: String,
        contextText: String,
        conversation: List<ChatMessage>,
        userMessage: String,
    ): Result<String> {
        SecureLogger.secureApiCall("OpenAI", "reply")
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
                ?: throw OperationException(OperationFailure.ProviderFailure(ErrorCode.PROVIDER_ERROR))
        }.mapFailure("reply")
    }
}

private fun <T> Result<T>.mapFailure(operation: String): Result<T> {
    return fold(
        onSuccess = {
            SecureLogger.secureApiResult("OpenAI", operation, true)
            Result.success(it)
        },
        onFailure = { error ->
            val failure = when (error) {
                is UnknownHostException -> {
                    SecureLogger.error("Network unreachable", ErrorCode.NETWORK_ERROR, error)
                    OperationFailure.NetworkUnavailable
                }

                is SocketTimeoutException -> {
                    SecureLogger.error("Request timeout", ErrorCode.REQUEST_TIMEOUT, error)
                    OperationFailure.ProviderFailure(ErrorCode.REQUEST_TIMEOUT)
                }

                is IOException -> {
                    SecureLogger.error("I/O error", ErrorCode.NETWORK_ERROR, error)
                    OperationFailure.NetworkUnavailable
                }

                is HttpException -> {
                    val errorCode = when (error.code()) {
                        401, 403 -> {
                            SecureLogger.error("Unauthorized", ErrorCode.INVALID_API_KEY)
                            ErrorCode.INVALID_API_KEY
                        }

                        429 -> {
                            SecureLogger.error("Rate limited", ErrorCode.RATE_LIMIT)
                            ErrorCode.RATE_LIMIT
                        }

                        else -> {
                            SecureLogger.error("HTTP error ${error.code()}", ErrorCode.PROVIDER_ERROR)
                            ErrorCode.PROVIDER_ERROR
                        }
                    }
                    OperationFailure.ProviderFailure(errorCode)
                }

                is OperationException -> {
                    SecureLogger.error(operation, error.failure.errorCode, error)
                    error.failure
                }

                else -> {
                    SecureLogger.error("Unexpected error", ErrorCode.UNEXPECTED_ERROR, error)
                    OperationFailure.Unexpected()
                }
            }
            SecureLogger.secureApiResult("OpenAI", operation, false)
            Result.failure(OperationException(failure, error))
        },
    )
}