package com.sumread.data.repository

import android.graphics.Bitmap
import com.sumread.data.local.TextRecognitionDataSource
import com.sumread.domain.model.OperationFailure
import com.sumread.domain.model.OperationException
import com.sumread.domain.repository.OcrRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OcrRepositoryImpl @Inject constructor(
    private val textRecognitionDataSource: TextRecognitionDataSource,
) : OcrRepository {

    override suspend fun extractText(bitmap: Bitmap): Result<String> {
        return runCatching {
            val text = textRecognitionDataSource.extractText(bitmap).trim()
            if (text.isBlank()) {
                throw OperationException(OperationFailure.EmptyText)
            }
            text
        }
    }
}
