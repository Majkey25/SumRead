package com.sumread

import com.sumread.domain.model.ErrorCode
import com.sumread.domain.model.OperationException
import com.sumread.domain.model.OperationFailure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorHandlingComprehensiveTest {

    @Test
    fun scenario_emptyUserInput_returnsCorrectFailure() {
        val failure = OperationFailure.EmptyText
        assertEquals(ErrorCode.EMPTY_TEXT, failure.errorCode)
        assertTrue(failure.userMessage.isNotBlank())
        assertFalse(failure.userMessage.contains("Exception"))
    }

    @Test
    fun scenario_missingApiKey_returnsCorrectFailure() {
        val failure = OperationFailure.MissingApiKey
        assertEquals(ErrorCode.MISSING_API_KEY, failure.errorCode)
        assertTrue(failure.userMessage.contains("API key"))
        assertTrue(failure.userMessage.contains("settings"))
    }

    @Test
    fun scenario_networkTimeout_returnsTimeoutError() {
        val exception = Exception("java.net.SocketTimeoutException")
        val failure = OperationFailure.ProviderFailure(ErrorCode.REQUEST_TIMEOUT)
        val operationEx = OperationException(failure, exception)

        assertEquals(ErrorCode.REQUEST_TIMEOUT, operationEx.failure.errorCode)
        assertEquals(exception, operationEx.cause)
        assertTrue(operationEx.message?.contains("long") == true)
    }

    @Test
    fun scenario_rateLimited_returnsCorrectMessage() {
        val failure = OperationFailure.ProviderFailure(ErrorCode.RATE_LIMIT)
        assertEquals(ErrorCode.RATE_LIMIT, failure.errorCode)
        assertTrue(failure.userMessage.isNotBlank())
    }

    @Test
    fun scenario_invalidApiKey_returnsSecureMessage() {
        val failure = OperationFailure.ProviderFailure(ErrorCode.INVALID_API_KEY)
        assertEquals(ErrorCode.INVALID_API_KEY, failure.errorCode)
        assertTrue(failure.userMessage.contains("invalid"))
        assertFalse(failure.userMessage.contains("401"))
        assertFalse(failure.userMessage.contains("403"))
    }

    @Test
    fun scenario_providerUnavailable_returnsGenericMessage() {
        val failure = OperationFailure.ProviderFailure(ErrorCode.PROVIDER_UNAVAILABLE)
        assertEquals(ErrorCode.PROVIDER_UNAVAILABLE, failure.errorCode)
        assertTrue(failure.userMessage.isNotBlank())
    }

    @Test
    fun scenario_unexpectedError_returnsGenericMessage() {
        val failure = OperationFailure.Unexpected()
        assertEquals(ErrorCode.UNEXPECTED_ERROR, failure.errorCode)
        assertTrue(failure.userMessage.contains("An error occurred"))
        assertFalse(failure.userMessage.lowercase().contains("null"))
    }

    @Test
    fun scenario_sessionExpired_returnsSessionError() {
        val failure = OperationFailure.Unexpected(ErrorCode.SESSION_ERROR)
        assertEquals(ErrorCode.SESSION_ERROR, failure.errorCode)
    }

    @Test
    fun scenario_ttsUnavailable_returnsDeviceSpecificMessage() {
        val failure = OperationFailure.TtsUnavailable
        assertEquals(ErrorCode.TTS_UNAVAILABLE, failure.errorCode)
        assertTrue(failure.userMessage.contains("device"))
        assertFalse(failure.userMessage.contains("install"))
    }

    @Test
    fun scenario_captureCancel_userInitiated() {
        val failure = OperationFailure.CaptureCancelled
        assertEquals(ErrorCode.CAPTURE_CANCELLED, failure.errorCode)
        assertTrue(failure.userMessage.contains("cancelled"))
    }

    @Test
    fun scenario_emptySelection_guideUserToAction() {
        val failure = OperationFailure.EmptySelection
        assertTrue(failure.userMessage.contains("Select"))
        assertTrue(failure.userMessage.contains("visible"))
    }

    @Test
    fun allErrorCodes_mappedToFailures() {
        val codeToFailure = mapOf(
            ErrorCode.EMPTY_SELECTION to OperationFailure.EmptySelection,
            ErrorCode.EMPTY_TEXT to OperationFailure.EmptyText,
            ErrorCode.MISSING_API_KEY to OperationFailure.MissingApiKey,
            ErrorCode.NETWORK_ERROR to OperationFailure.NetworkUnavailable,
            ErrorCode.TTS_UNAVAILABLE to OperationFailure.TtsUnavailable,
            ErrorCode.CAPTURE_CANCELLED to OperationFailure.CaptureCancelled,
        )

        codeToFailure.forEach { (code, failure) ->
            assertEquals(code, failure.errorCode)
            assertTrue("Message required for $code", failure.userMessage.isNotBlank())
        }
    }

    @Test
    fun allErrorCodes_inRange() {
        ErrorCode.entries.forEach { code ->
            assertTrue("Code must be >= 1001: ${code.code}", code.code >= 1001)
            assertTrue("Code must be <= 9999: ${code.code}", code.code <= 9999)
        }
    }

    @Test
    fun errorChain_preservesCauseContext() {
        val originalException = Exception("Original network error")
        val operationFailure = OperationFailure.NetworkUnavailable
        val operationException = OperationException(operationFailure, originalException)

        assertEquals(operationFailure, operationException.failure)
        assertEquals(originalException, operationException.cause)
        assertNotNull(operationException.cause?.message)
    }

    @Test
    fun errorChain_multipleProviders_allReturnSameCodeForSameError() {
        val groqError = OperationFailure.ProviderFailure(ErrorCode.RATE_LIMIT)
        val openaiError = OperationFailure.ProviderFailure(ErrorCode.RATE_LIMIT)
        val geminiError = OperationFailure.ProviderFailure(ErrorCode.RATE_LIMIT)

        assertEquals(groqError.errorCode, openaiError.errorCode)
        assertEquals(openaiError.errorCode, geminiError.errorCode)
        assertEquals(groqError.userMessage, openaiError.userMessage)
        assertEquals(openaiError.userMessage, geminiError.userMessage)
    }

    @Test
    fun userMessages_consistent_acrossInstances() {
        val failure1 = OperationFailure.MissingApiKey
        val failure2 = OperationFailure.MissingApiKey

        assertEquals(failure1.userMessage, failure2.userMessage)
        assertEquals(failure1.errorCode, failure2.errorCode)
    }

    @Test
    fun providerErrors_differentCodes_differentMessages() {
        val invalidKeyError = OperationFailure.ProviderFailure(ErrorCode.INVALID_API_KEY)
        val rateLimitError = OperationFailure.ProviderFailure(ErrorCode.RATE_LIMIT)
        val timeoutError = OperationFailure.ProviderFailure(ErrorCode.REQUEST_TIMEOUT)

        assertTrue(invalidKeyError.userMessage.isNotBlank())
        assertTrue(rateLimitError.userMessage.isNotBlank())
        assertTrue(timeoutError.userMessage.isNotBlank())

        assertTrue("Messages should differ", invalidKeyError.userMessage.lowercase() != rateLimitError.userMessage.lowercase())
        assertTrue("Messages should differ", rateLimitError.userMessage.lowercase() != timeoutError.userMessage.lowercase())
    }

    @Test
    fun errorMessages_noTechnicalLeakage() {
        val failures = listOf(
            OperationFailure.ProviderFailure(ErrorCode.INVALID_API_KEY),
            OperationFailure.ProviderFailure(ErrorCode.RATE_LIMIT),
            OperationFailure.ProviderFailure(ErrorCode.NETWORK_ERROR),
            OperationFailure.ProviderFailure(ErrorCode.REQUEST_TIMEOUT),
            OperationFailure.ProviderFailure(ErrorCode.PROVIDER_ERROR),
        )

        failures.forEach { failure ->
            val msg = failure.userMessage.lowercase()
            assertFalse("Should not contain HTTP codes", msg.contains("401"))
            assertFalse("Should not contain HTTP codes", msg.contains("403"))
            assertFalse("Should not contain HTTP codes", msg.contains("429"))
            assertFalse("Should not contain HTTP codes", msg.contains("500"))
            assertFalse("Should not contain Exception class", msg.contains("exception"))
            assertFalse("Should not contain Stack", msg.contains("stack"))
        }
    }

    @Test
    fun failureResult_wrappedInException() {
        val failure = OperationFailure.EmptyText
        val exception = OperationException(failure)

        assertTrue(exception is OperationException)
        assertEquals(failure, exception.failure)
        assertEquals(failure.userMessage, exception.message)
    }

    @Test
    fun unexpected_canBeCustomized() {
        val session = OperationFailure.Unexpected(ErrorCode.SESSION_ERROR)
        val storage = OperationFailure.Unexpected(ErrorCode.STORAGE_ERROR)
        val encryption = OperationFailure.Unexpected(ErrorCode.ENCRYPTION_ERROR)

        assertEquals(ErrorCode.SESSION_ERROR, session.errorCode)
        assertEquals(ErrorCode.STORAGE_ERROR, storage.errorCode)
        assertEquals(ErrorCode.ENCRYPTION_ERROR, encryption.errorCode)
    }

    private fun assertNotEqual(value1: String, value2: String) {
        assertTrue("Values should be different", value1 != value2)
    }
}






