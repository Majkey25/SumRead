package com.sumread.presentation.capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.sumread.domain.model.CaptureSelection
import com.sumread.util.RectUtils
import kotlin.math.max

class RegionSelectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var onSelectionChanged: (CaptureSelection?) -> Unit = {}

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dimPaint = Paint().apply { color = Color.argb(170, 8, 12, 18) }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density * 2
    }

    private var bitmap: Bitmap? = null
    private val drawnBitmapRect = RectF()
    private var selectedRect: RectF? = null
    private var startX = 0f
    private var startY = 0f

    fun setBitmap(value: Bitmap?) {
        if (bitmap === value) {
            return
        }
        bitmap = value
        selectedRect = null
        onSelectionChanged(null)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val source = bitmap ?: return
        updateDrawnBitmapRect(source)
        canvas.drawColor(Color.BLACK)
        canvas.drawBitmap(source, null, drawnBitmapRect, bitmapPaint)
        selectedRect?.let { rect ->
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)
            canvas.save()
            canvas.clipRect(rect)
            canvas.drawBitmap(source, null, drawnBitmapRect, bitmapPaint)
            canvas.restore()
            canvas.drawRect(rect, strokePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val source = bitmap ?: return false
        if (!drawnBitmapRect.contains(event.x, event.y) && event.actionMasked == MotionEvent.ACTION_DOWN) {
            return false
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                selectedRect = RectF(startX, startY, startX, startY)
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP -> {
                val updatedRect = RectUtils.normalizedRect(
                    startX = startX,
                    startY = startY,
                    endX = event.x,
                    endY = event.y,
                    bounds = drawnBitmapRect,
                )
                selectedRect = updatedRect
                val mappedSelection = if (
                    max(updatedRect.width(), updatedRect.height()) >= resources.displayMetrics.density * 24
                ) {
                    RectUtils.mapToBitmap(
                        selectedRect = updatedRect,
                        drawnBitmapRect = drawnBitmapRect,
                        bitmapWidth = source.width,
                        bitmapHeight = source.height,
                    )
                } else {
                    null
                }
                onSelectionChanged(mappedSelection)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateDrawnBitmapRect(source: Bitmap) {
        val scale = minOf(
            width / source.width.toFloat(),
            height / source.height.toFloat(),
        )
        val drawnWidth = source.width * scale
        val drawnHeight = source.height * scale
        val left = (width - drawnWidth) / 2
        val top = (height - drawnHeight) / 2
        drawnBitmapRect.set(left, top, left + drawnWidth, top + drawnHeight)
    }
}
