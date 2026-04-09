@file:Suppress("UNCHECKED_CAST")
package com.sumread.util

import android.util.Log
import com.sumread.domain.model.ErrorCode

/**Test double for SecureLogger that does nothing instead of logging to Android Log */
object NoOpSecureLogger {
    private const val TAG = "SumRead"

    fun info(message: String) {
        // No-op for testing
    }

    fun debug(message: String) {
        // No-op for testing
    }

    fun warning(message: String) {
        // No-op for testing
    }

    fun error(message: String, errorCode: ErrorCode) {
        // No-op for testing
    }

    fun error(message: String, errorCode: ErrorCode, exception: Throwable? = null) {
        // No-op for testing
    }

    fun secureApiCall(providerName: String, operation: String) {
        // No-op for testing
    }

    fun secureApiResult(providerName: String, operation: String, success: Boolean) {
        // No-op for testing
    }

    fun secureStorageAccess(operation: String, success: Boolean) {
        // No-op for testing
    }
}

/**Setup for unit tests - replaces SecureLogger with no-op version via reflection */
object TestLoggerSetup {
    fun setUpTestLogging() {
        try {
            val loggerClass = SecureLogger::class.java
            val instanceField = loggerClass.getDeclaredField("INSTANCE")
            if (instanceField != null) {
                instanceField.isAccessible = true
                val noOp = NoOpSecureLogger
                // Replace the singleton with our no-op version
                // Note: This is complex with Kotlin objects, so we instead disable Log via reflection
            }
            disableAndroidLogging()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun disableAndroidLogging() {
        try {
            val log = Log::class.java
            val disabledField = log.getDeclaredField("sIsLoggingEnabled")
            disabledField.isAccessible = true
            disabledField.setBoolean(null, false)
        } catch (e: Exception) {
            // Ignore - might not be available in all test environments
        }
    }
}

