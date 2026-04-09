package com.sumread

import com.google.common.truth.Truth.assertThat
import com.sumread.util.CaptureFramePolicy
import org.junit.Test

class CaptureFramePolicyTest {

    @Test
    fun `first frame is ignored to avoid transient blank capture`() {
        assertThat(CaptureFramePolicy.shouldProcessFrame(1)).isFalse()
    }

    @Test
    fun `second frame is accepted for capture`() {
        assertThat(CaptureFramePolicy.shouldProcessFrame(2)).isTrue()
    }

    @Test
    fun `later frames stay accepted`() {
        assertThat(CaptureFramePolicy.shouldProcessFrame(8)).isTrue()
    }
}

