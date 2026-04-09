package com.sumread

import com.sumread.domain.model.ErrorCode
import com.sumread.domain.model.OperationException
import com.sumread.domain.model.OperationFailure
import com.sumread.domain.model.toErrorCode
import com.sumread.domain.model.toFailureMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorHandlingTest {

    @Test
    fun errorCodes_allHaveUniqueValues() {
        val codes = ErrorCode.entries.map { it.code }
        assertEquals("ErrorCode numbers must be unique", codes.size, codes.toSet().size)
    }

    @Test
    fun errorCodes_allHaveDescriptions() {
        ErrorCode.entries.forEach { code ->
            assertTrue("ErrorCode ${code.name} missing description", code.description.isNotBlank())
        }
    }

    @Test
    fun operationFailure_userMessagesNeverBlank() {
        val failures = listOf(
            OperationFailure.EmptySelection,
            OperationFailure.EmptyText,
            OperationFailure.MissingApiKey,
            OperationFailure.NetworkUnavailable,
            OperationFailure.TtsUnavailable,
            OperationFailure.CaptureCancelled,
            OperationFailure.ProviderFailure(ErrorCode.INVALID_API_KEY),
            OperationFailure.ProviderFailure(ErrorCode.PROVIDER_ERROR),
            OperationFailure.Unexpected(),
        )

        failures.forEach { failure ->
            assertTrue("Message must not be blank: $failure", failure.userMessage.isNotBlank())
            assertTrue("Message must be user-friendly: ${failure.userMessage}", !failure.userMessage.contains("Exception"))
        }
    }

    @Test
    fun operationException_preservesFailure() {
        val failure = OperationFailure.MissingApiKey
        val exception = OperationException(failure)
        assertEquals(failure, exception.failure)
        assertEquals(failure.userMessage, exception.message)
    }

    @Test
    fun operationException_canWrapCause() {
        val cause = Exception("Network timeout")
        val exception = OperationException(OperationFailure.NetworkUnavailable, cause)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun toErrorCode_extractsCodeFromException() {
        val exception = OperationException(OperationFailure.MissingApiKey)
        val code = exception.toErrorCode()
        assertEquals(ErrorCode.MISSING_API_KEY, code)
    }

    @Test
    fun toFailureMessage_extractsMessageFromException() {
        val exception = OperationException(OperationFailure.MissingApiKey)
        val message = exception.toFailureMessage()
        assertEquals(OperationFailure.MissingApiKey.userMessage, message)
    }

    @Test
    fun emptySelection_hasCorrectErrorCode() {
        assertEquals(ErrorCode.EMPTY_SELECTION, OperationFailure.EmptySelection.errorCode)
    }

    @Test
    fun missingApiKey_hasCorrectErrorCode() {
        assertEquals(ErrorCode.MISSING_API_KEY, OperationFailure.MissingApiKey.errorCode)
    }

    @Test
    fun networkUnavailable_hasCorrectErrorCode() {
        assertEquals(ErrorCode.NETWORK_ERROR, OperationFailure.NetworkUnavailable.errorCode)
    }

    @Test
    fun providerFailureInvalidKey_hasErrorCode() {
        val failure = OperationFailure.ProviderFailure(ErrorCode.INVALID_API_KEY)
        assertEquals(ErrorCode.INVALID_API_KEY, failure.errorCode)
    }

    @Test
    fun unexpected_hasErrorCode() {
        val failure = OperationFailure.Unexpected(ErrorCode.SESSION_ERROR)
        assertEquals(ErrorCode.SESSION_ERROR, failure.errorCode)
    }

    @Test
    fun allFailures_haveErrorCodes() {
        val failures = listOf(
            OperationFailure.EmptySelection,
            OperationFailure.EmptyText,
            OperationFailure.MissingApiKey,
            OperationFailure.NetworkUnavailable,
            OperationFailure.TtsUnavailable,
            OperationFailure.CaptureCancelled,
            OperationFailure.ProviderFailure(ErrorCode.PROVIDER_ERROR),
            OperationFailure.Unexpected(),
        )

        failures.forEach { failure ->
            assertTrue("All failures must have error codes", failure.errorCode.code > 0)
        }
    }
}

