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
class GroqAiProvider @Inject constructor(
    private val groqApiService: GroqApiService,
) : AiProvider {

    override val type: AiProviderType = AiProviderType.GROQ

    override suspend fun summarize(apiKey: String, model: String, sourceText: String): Result<String> {
        SecureLogger.secureApiCall("Groq", "summarize")
        return runCatching {
            val response = groqApiService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = GroqChatRequest(
                    model = model,
                    temperature = 0.2f,
                    messages = listOf(
                        GroqMessage(role = "system", content = AiPromptFactory.summarySystemPrompt()),
                        GroqMessage(role = "user", content = AiPromptFactory.summaryUserPrompt(sourceText)),
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
        SecureLogger.secureApiCall("Groq", "reply")
        return runCatching {
            val response = groqApiService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = GroqChatRequest(
                    model = model,
                    temperature = 0.3f,
                    messages = AiPromptFactory.chatMessages(contextText, conversation, userMessage)
                        .map { GroqMessage(role = it.role, content = it.content) },
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
            SecureLogger.secureApiResult("Groq", operation, true)
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
            SecureLogger.secureApiResult("Groq", operation, false)
            Result.failure(OperationException(failure, error))
        },
    )
}
