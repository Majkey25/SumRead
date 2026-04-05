package com.sumread.domain.usecase

import com.sumread.domain.model.OperationFailure
import com.sumread.domain.model.OperationException
import com.sumread.domain.model.failureResult
import com.sumread.domain.repository.AiRepository
import com.sumread.domain.repository.SpeechRepository
import javax.inject.Inject

class SummarizeSelectionUseCase @Inject constructor(
    private val aiRepository: AiRepository,
    private val speechRepository: SpeechRepository,
) {
    suspend operator fun invoke(text: String): Result<String> {
        val sanitized = text.trim()
        if (sanitized.isBlank()) {
            return failureResult(OperationFailure.EmptyText)
        }
        return aiRepository.summarize(sanitized).mapCatching { summary ->
            val normalizedSummary = summary.trim()
            if (normalizedSummary.isBlank()) {
                throw OperationException(OperationFailure.EmptyText)
            }
            speechRepository.speak(normalizedSummary).getOrThrow()
            normalizedSummary
        }
    }
}
