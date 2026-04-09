package com.sumread.util

import android.util.Log
import com.sumread.domain.model.ErrorCode

object SecureLogger {
    private const val TAG = "SumRead"

    fun info(message: String) {
        Log.i(TAG, message)
    }

    fun debug(message: String) {
        Log.d(TAG, message)
    }

    fun warning(message: String) {
        Log.w(TAG, message)
    }

    fun error(message: String, errorCode: ErrorCode) {
        Log.e(TAG, "[$errorCode] $message")
    }

    fun error(message: String, errorCode: ErrorCode, exception: Throwable? = null) {
        if (exception != null) {
            Log.e(TAG, "[$errorCode] $message", exception)
        } else {
            Log.e(TAG, "[$errorCode] $message")
        }
    }

    fun secureApiCall(providerName: String, operation: String) {
        Log.d(TAG, "[$providerName] $operation called")
    }

    fun secureApiResult(providerName: String, operation: String, success: Boolean) {
        Log.d(TAG, "[$providerName] $operation result: ${if (success) "success" else "failure"}")
    }

    fun secureStorageAccess(operation: String, success: Boolean) {
        Log.d(TAG, "[Storage] $operation: ${if (success) "success" else "failure"}")
    }
}

