package com.sumread.domain.repository

import com.sumread.domain.model.ChatMessage

interface AiRepository {
    suspend fun summarize(sourceText: String): Result<String>
    suspend fun reply(
        contextText: String,
        conversation: List<ChatMessage>,
        userMessage: String,
    ): Result<String>
}
