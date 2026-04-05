package com.sumread.data.repository

import android.content.Context
import androidx.core.content.ContextCompat
import com.sumread.domain.repository.OverlayController
import com.sumread.service.FloatingService
import com.sumread.util.IntentFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

@Singleton
class OverlayControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : OverlayController {

    override val isRunning: StateFlow<Boolean> = FloatingService.runningState

    override fun start() {
        ContextCompat.startForegroundService(context, IntentFactory.overlayService(context))
    }

    override fun stop() {
        context.startService(IntentFactory.stopOverlayService(context))
    }
}
