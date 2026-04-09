package com.sumread

import org.junit.BeforeClass
import android.util.Log

object TestSetup {
    @BeforeClass
    fun setUp() {
        try {
            val logClass = Log::class.java
            val intentField = logClass.getDeclaredField("sIsLoggingEnabled")
            intentField.isAccessible = true
            intentField.setBoolean(null, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

