package com.sumread.data.repository

import com.sumread.domain.model.ChatMessage
import com.sumread.domain.model.ChatRole
import com.sumread.domain.model.ChatSession
import com.sumread.domain.repository.ChatSessionRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class ChatSessionRepositoryImpl @Inject constructor() : ChatSessionRepository {

    private val sessionState = MutableStateFlow<ChatSession?>(null)

    override val session: StateFlow<ChatSession?> = sessionState

    override fun start(contextText: String) {
        sessionState.value = ChatSession(
            contextText = contextText,
            messages = emptyList(),
        )
    }

    override fun addUserMessage(text: String) {
        appendMessage(role = ChatRole.USER, text = text)
    }

    override fun addAssistantMessage(text: String) {
        appendMessage(role = ChatRole.ASSISTANT, text = text)
    }

    override fun clear() {
        sessionState.value = null
    }

    private fun appendMessage(role: ChatRole, text: String) {
        val current = sessionState.value ?: return
        sessionState.value = current.copy(
            messages = current.messages + ChatMessage(
                id = UUID.randomUUID().toString(),
                role = role,
                text = text,
            ),
        )
    }
}
