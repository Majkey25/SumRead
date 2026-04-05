package com.sumread.domain.usecase

import com.sumread.domain.model.OperationFailure
import com.sumread.domain.model.failureResult
import com.sumread.domain.repository.SpeechRepository
import javax.inject.Inject

class SpeakSelectionUseCase @Inject constructor(
    private val speechRepository: SpeechRepository,
) {
    suspend operator fun invoke(text: String): Result<String> {
        val sanitized = text.trim()
        if (sanitized.isBlank()) {
            return failureResult(OperationFailure.EmptyText)
        }
        return speechRepository.speak(sanitized).map { sanitized }
    }
}
