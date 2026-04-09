package com.sumread

import com.google.common.truth.Truth.assertThat
import com.sumread.util.AppConfig
import org.junit.Test

class OverlayActionsLayoutTest {

    @Test
    fun `action panel offset keeps panel separated from bubble`() {
        assertThat(AppConfig.actionPanelOffsetX).isGreaterThan(0)
    }

    @Test
    fun `overlay bubble starts within visible screen bounds`() {
        assertThat(AppConfig.overlayStartX).isAtLeast(0)
        assertThat(AppConfig.overlayStartY).isAtLeast(0)
    }

    @Test
    fun `touch slop scale keeps drag detection active`() {
        assertThat(AppConfig.overlayTouchSlopScale).isAtLeast(1)
    }
}



