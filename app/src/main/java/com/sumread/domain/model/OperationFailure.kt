package com.sumread.domain.model

enum class ErrorCode(val code: Int, val description: String) {
    EMPTY_SELECTION(1001, "Selection empty"),
    EMPTY_TEXT(1002, "No text found"),
    MISSING_API_KEY(2001, "API key not configured"),
    INVALID_API_KEY(2002, "Invalid API key"),
    NETWORK_ERROR(3001, "Network unavailable"),
    REQUEST_TIMEOUT(3002, "Request timeout"),
    RATE_LIMIT(3003, "Rate limit exceeded"),
    PROVIDER_ERROR(4001, "Provider error"),
    PROVIDER_UNAVAILABLE(4002, "Provider unavailable"),
    TTS_UNAVAILABLE(5001, "TTS unavailable"),
    OCR_FAILED(5002, "Text recognition failed"),
    CAPTURE_CANCELLED(6001, "Capture cancelled"),
    STORAGE_ERROR(7001, "Storage error"),
    ENCRYPTION_ERROR(7002, "Encryption error"),
    SESSION_ERROR(8001, "Session error"),
    UNEXPECTED_ERROR(9999, "An error occurred"),
}

sealed interface OperationFailure {
    val userMessage: String
    val errorCode: ErrorCode

    data object EmptySelection : OperationFailure {
        override val userMessage = "Select a visible text region first."
        override val errorCode = ErrorCode.EMPTY_SELECTION
    }

    data object EmptyText : OperationFailure {
        override val userMessage = "No readable text was found in the selected area."
        override val errorCode = ErrorCode.EMPTY_TEXT
    }

    data object MissingApiKey : OperationFailure {
        override val userMessage = "Add an API key for the selected provider in settings."
        override val errorCode = ErrorCode.MISSING_API_KEY
    }

    data object NetworkUnavailable : OperationFailure {
        override val userMessage = "An internet connection is required for the selected AI mode."
        override val errorCode = ErrorCode.NETWORK_ERROR
    }

    data object TtsUnavailable : OperationFailure {
        override val userMessage = "Text to speech is not available on this device."
        override val errorCode = ErrorCode.TTS_UNAVAILABLE
    }

    data object CaptureCancelled : OperationFailure {
        override val userMessage = "The screen capture request was cancelled."
        override val errorCode = ErrorCode.CAPTURE_CANCELLED
    }

    data class ProviderFailure(val providerErrorCode: ErrorCode) : OperationFailure {
        override val errorCode = providerErrorCode
        override val userMessage = when (providerErrorCode) {
            ErrorCode.INVALID_API_KEY -> "The API key is invalid. Check your settings."
            ErrorCode.RATE_LIMIT -> "Too many requests. Please try again in a moment."
            ErrorCode.PROVIDER_UNAVAILABLE -> "The service is temporarily unavailable."
            ErrorCode.PROVIDER_ERROR -> "The AI service encountered an error."
            ErrorCode.REQUEST_TIMEOUT -> "The request took too long. Please try again."
            else -> "The service is temporarily unavailable. Please try again."
        }
    }

    data class Unexpected(override val errorCode: ErrorCode = ErrorCode.UNEXPECTED_ERROR) : OperationFailure {
        override val userMessage = "An error occurred. Please try again."
    }
}

class OperationException(val failure: OperationFailure, cause: Throwable? = null) :
    IllegalStateException(failure.userMessage, cause)

fun failureResult(failure: OperationFailure): Result<Nothing> {
    return Result.failure(OperationException(failure))
}

fun Throwable.toFailureMessage(): String {
    return (this as? OperationException)?.failure?.userMessage ?: (message ?: "Unexpected error")
}

fun Throwable.toErrorCode(): ErrorCode {
    return (this as? OperationException)?.failure?.errorCode ?: ErrorCode.UNEXPECTED_ERROR
}
