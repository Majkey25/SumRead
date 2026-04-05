package com.sumread.domain.usecase

import com.sumread.domain.repository.OverlayController
import javax.inject.Inject

class StopOverlayUseCase @Inject constructor(
    private val overlayController: OverlayController,
) {
    operator fun invoke() {
        overlayController.stop()
    }
}
