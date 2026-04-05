package com.sumread.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface OverlayController {
    val isRunning: StateFlow<Boolean>

    fun start()
    fun stop()
}
