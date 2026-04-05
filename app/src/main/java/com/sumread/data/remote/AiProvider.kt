package com.sumread.data.remote

import com.sumread.domain.model.AiProviderType
import com.sumread.domain.model.ChatMessage

interface AiProvider {
    val type: AiProviderType

    suspend fun summarize(apiKey: String, sourceText: String): Result<String>

    suspend fun reply(
        apiKey: String,
        contextText: String,
        conversation: List<ChatMessage>,
        userMessage: String,
    ): Result<String>
}
