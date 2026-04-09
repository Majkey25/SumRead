package com.sumread.util

object CaptureFramePolicy {
    private const val minStableFrameCount = 2

    fun shouldProcessFrame(capturedCount: Int): Boolean {
        return capturedCount >= minStableFrameCount
    }
}

