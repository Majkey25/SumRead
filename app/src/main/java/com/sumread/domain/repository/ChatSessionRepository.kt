package com.sumread.domain.repository

import com.sumread.domain.model.ChatSession
import kotlinx.coroutines.flow.StateFlow

interface ChatSessionRepository {
    val session: StateFlow<ChatSession?>

    fun start(contextText: String)
    fun addUserMessage(text: String)
    fun addAssistantMessage(text: String)
    fun clear()
}
