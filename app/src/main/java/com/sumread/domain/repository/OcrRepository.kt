package com.sumread.domain.repository

import android.graphics.Bitmap

interface OcrRepository {
    suspend fun extractText(bitmap: Bitmap): Result<String>
}
