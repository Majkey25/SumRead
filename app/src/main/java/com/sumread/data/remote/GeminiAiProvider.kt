package com.sumread.data.remote

import com.sumread.domain.model.AiProviderType
import com.sumread.domain.model.ChatMessage
import com.sumread.domain.model.ChatRole
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
class GeminiAiProvider @Inject constructor(
    private val geminiApiService: GeminiApiService,
) : AiProvider {

    override val type: AiProviderType = AiProviderType.GEMINI

    override suspend fun summarize(apiKey: String, model: String, sourceText: String): Result<String> {
        SecureLogger.secureApiCall("Gemini", "summarize")
        return runCatching {
            val response = geminiApiService.generateContent(
                model = model,
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
        SecureLogger.secureApiCall("Gemini", "reply")
        return runCatching {
            val response = geminiApiService.generateContent(
                model = model,
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
                ?: throw OperationException(OperationFailure.ProviderFailure(ErrorCode.PROVIDER_ERROR))
        }.mapFailure("reply")
    }
}

private fun <T> Result<T>.mapFailure(operation: String): Result<T> {
    return fold(
        onSuccess = {
            SecureLogger.secureApiResult("Gemini", operation, true)
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
            SecureLogger.secureApiResult("Gemini", operation, false)
            Result.failure(OperationException(failure, error))
        },
    )
}
