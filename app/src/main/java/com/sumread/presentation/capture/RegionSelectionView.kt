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
import android.view.ViewConfiguration
import com.sumread.domain.model.CaptureSelection
import com.sumread.util.RectUtils
import kotlin.math.abs
import kotlin.math.max

class RegionSelectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var onSelectionChanged: (CaptureSelection?) -> Unit = {}

    private val density = context.resources.displayMetrics.density
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()

    // Tap auto-expand size in dp (wide paragraph strip)
    private val tapExpandWidthDp = 300f
    private val tapExpandHeightDp = 120f

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dimPaint = Paint().apply { color = Color.argb(170, 8, 12, 18) }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = density * 2
    }
    private val tapHintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(200, 255, 255, 255)
        textSize = density * 13f
        textAlign = Paint.Align.CENTER
    }

    private var bitmap: Bitmap? = null
    private val drawnBitmapRect = RectF()
    private var selectedRect: RectF? = null
    private var startX = 0f
    private var startY = 0f
    private var dragging = false
    private var hasSelection = false

    fun setBitmap(value: Bitmap?) {
        if (bitmap === value) return
        bitmap = value
        selectedRect = null
        hasSelection = false
        onSelectionChanged(null)
        invalidate()
    }

    /** Selects the entire drawn bitmap area — used by the "Full screen" button. */
    fun selectAll() {
        val source = bitmap ?: return
        val rect = RectF(drawnBitmapRect)
        selectedRect = rect
        hasSelection = true
        onSelectionChanged(
            RectUtils.mapToBitmap(
                selectedRect = rect,
                drawnBitmapRect = drawnBitmapRect,
                bitmapWidth = source.width,
                bitmapHeight = source.height,
            ),
        )
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val source = bitmap ?: return
        updateDrawnBitmapRect(source)

        canvas.drawColor(Color.BLACK)
        canvas.drawBitmap(source, null, drawnBitmapRect, bitmapPaint)

        val rect = selectedRect
        if (rect != null) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)
            canvas.save()
            canvas.clipRect(rect)
            canvas.drawBitmap(source, null, drawnBitmapRect, bitmapPaint)
            canvas.restore()
            canvas.drawRect(rect, strokePaint)
        } else {
            // Hint text when no selection yet
            canvas.drawText(
                "Tap to select area  •  Drag for precise selection",
                width / 2f,
                drawnBitmapRect.bottom + density * 20f,
                tapHintPaint,
            )
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
                dragging = false
                selectedRect = RectF(startX, startY, startX, startY)
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = abs(event.x - startX)
                val deltaY = abs(event.y - startY)
                if (!dragging && (deltaX > touchSlop || deltaY > touchSlop)) {
                    dragging = true
                }
                if (dragging) {
                    val updatedRect = RectUtils.normalizedRect(
                        startX = startX,
                        startY = startY,
                        endX = event.x,
                        endY = event.y,
                        bounds = drawnBitmapRect,
                    )
                    selectedRect = updatedRect
                    onSelectionChanged(
                        if (max(updatedRect.width(), updatedRect.height()) >= density * 24) {
                            RectUtils.mapToBitmap(
                                selectedRect = updatedRect,
                                drawnBitmapRect = drawnBitmapRect,
                                bitmapWidth = source.width,
                                bitmapHeight = source.height,
                            )
                        } else {
                            null
                        },
                    )
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (!dragging) {
                    // Tap: auto-expand a region around the tap point
                    val tapRect = expandTapToRect(event.x, event.y)
                    selectedRect = tapRect
                    hasSelection = true
                    onSelectionChanged(
                        RectUtils.mapToBitmap(
                            selectedRect = tapRect,
                            drawnBitmapRect = drawnBitmapRect,
                            bitmapWidth = source.width,
                            bitmapHeight = source.height,
                        ),
                    )
                } else {
                    // Drag ended — emit final mapped selection
                    val finalRect = selectedRect
                    if (finalRect != null && max(finalRect.width(), finalRect.height()) >= density * 24) {
                        hasSelection = true
                        onSelectionChanged(
                            RectUtils.mapToBitmap(
                                selectedRect = finalRect,
                                drawnBitmapRect = drawnBitmapRect,
                                bitmapWidth = source.width,
                                bitmapHeight = source.height,
                            ),
                        )
                    }
                }
                invalidate()
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun expandTapToRect(x: Float, y: Float): RectF {
        val halfW = tapExpandWidthDp * density / 2f
        val halfH = tapExpandHeightDp * density / 2f
        return RectF(
            (x - halfW).coerceAtLeast(drawnBitmapRect.left),
            (y - halfH).coerceAtLeast(drawnBitmapRect.top),
            (x + halfW).coerceAtMost(drawnBitmapRect.right),
            (y + halfH).coerceAtMost(drawnBitmapRect.bottom),
        )
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
