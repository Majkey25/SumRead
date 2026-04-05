package com.sumread.domain.usecase

import com.sumread.domain.model.OperationFailure
import com.sumread.domain.model.failureResult
import com.sumread.domain.repository.AiRepository
import com.sumread.domain.repository.ChatSessionRepository
import javax.inject.Inject

class SubmitChatMessageUseCase @Inject constructor(
    private val aiRepository: AiRepository,
    private val chatSessionRepository: ChatSessionRepository,
) {
    suspend operator fun invoke(message: String): Result<String> {
        val trimmedMessage = message.trim()
        if (trimmedMessage.isBlank()) {
            return failureResult(OperationFailure.EmptyText)
        }

        val session = chatSessionRepository.session.value
            ?: return failureResult(OperationFailure.Unexpected("Start a chat session from a screen capture first."))

        chatSessionRepository.addUserMessage(trimmedMessage)
        return aiRepository.reply(
            contextText = session.contextText,
            conversation = session.messages,
            userMessage = trimmedMessage,
        ).map { reply ->
            val normalizedReply = reply.trim()
            chatSessionRepository.addAssistantMessage(normalizedReply)
            normalizedReply
        }
    }
}
