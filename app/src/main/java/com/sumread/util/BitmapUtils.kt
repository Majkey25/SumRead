package com.sumread.util

import android.graphics.Bitmap
import android.media.Image
import com.sumread.domain.model.CaptureSelection

object BitmapUtils {

    fun crop(source: Bitmap, selection: CaptureSelection): Bitmap {
        return Bitmap.createBitmap(
            source,
            selection.left,
            selection.top,
            selection.width,
            selection.height,
        )
    }

    fun fromImage(image: Image): Bitmap {
        val width = image.width
        val height = image.height
        val plane = image.planes.first()
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width
        val padded = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888,
        )
        padded.copyPixelsFromBuffer(buffer)
        val cropped = Bitmap.createBitmap(padded, 0, 0, width, height)
        padded.recycle()
        return cropped
    }
}
