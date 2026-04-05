package com.sumread.domain.model

sealed interface OperationFailure {
    val userMessage: String

    data object EmptySelection : OperationFailure {
        override val userMessage = "Select a visible text region first."
    }

    data object EmptyText : OperationFailure {
        override val userMessage = "No readable text was found in the selected area."
    }

    data object MissingApiKey : OperationFailure {
        override val userMessage = "Add an API key for the selected provider in settings."
    }

    data object NetworkUnavailable : OperationFailure {
        override val userMessage = "An internet connection is required for the selected AI mode."
    }

    data object TtsUnavailable : OperationFailure {
        override val userMessage = "Text to speech is not available on this device."
    }

    data object CaptureCancelled : OperationFailure {
        override val userMessage = "The screen capture request was cancelled."
    }

    data class ProviderFailure(val detail: String) : OperationFailure {
        override val userMessage = detail
    }

    data class Unexpected(val detail: String) : OperationFailure {
        override val userMessage = detail
    }
}

class OperationException(val failure: OperationFailure) : IllegalStateException(failure.userMessage)

fun failureResult(failure: OperationFailure): Result<Nothing> {
    return Result.failure(OperationException(failure))
}

fun Throwable.toFailureMessage(): String {
    return (this as? OperationException)?.failure?.userMessage ?: (message ?: "Unexpected error")
}
