package com.sumread.util

import android.graphics.RectF
import com.sumread.domain.model.CaptureSelection
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object RectUtils {

    fun normalizedRect(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        bounds: RectF,
    ): RectF {
        val left = min(startX, endX).coerceIn(bounds.left, bounds.right)
        val top = min(startY, endY).coerceIn(bounds.top, bounds.bottom)
        val right = max(startX, endX).coerceIn(bounds.left, bounds.right)
        val bottom = max(startY, endY).coerceIn(bounds.top, bounds.bottom)
        return RectF(left, top, right, bottom)
    }

    fun mapToBitmap(
        selectedRect: RectF,
        drawnBitmapRect: RectF,
        bitmapWidth: Int,
        bitmapHeight: Int,
    ): CaptureSelection {
        val normalized = RectF(selectedRect).apply {
            intersect(drawnBitmapRect)
        }
        val horizontalScale = bitmapWidth / drawnBitmapRect.width()
        val verticalScale = bitmapHeight / drawnBitmapRect.height()
        val left = ((normalized.left - drawnBitmapRect.left) * horizontalScale).roundToInt()
            .coerceIn(0, bitmapWidth - 1)
        val top = ((normalized.top - drawnBitmapRect.top) * verticalScale).roundToInt()
            .coerceIn(0, bitmapHeight - 1)
        val right = ((normalized.right - drawnBitmapRect.left) * horizontalScale).roundToInt()
            .coerceIn(left + 1, bitmapWidth)
        val bottom = ((normalized.bottom - drawnBitmapRect.top) * verticalScale).roundToInt()
            .coerceIn(top + 1, bitmapHeight)
        return CaptureSelection(
            left = left,
            top = top,
            right = right,
            bottom = bottom,
        )
    }
}
