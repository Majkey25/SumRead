package com.sumread.util

import android.util.Log
import java.lang.reflect.Field

object TestLoggerHelper {
    fun disableLogging() {
        try {
            val field: Field = Log::class.java.getDeclaredField("sIsLoggingEnabled")
            field.isAccessible = true
            field.setBoolean(null, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

