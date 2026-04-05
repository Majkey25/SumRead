package com.sumread.domain.model

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val text: String,
)

enum class ChatRole {
    USER,
    ASSISTANT,
}
