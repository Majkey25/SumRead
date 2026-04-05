package com.sumread.domain.model

data class ChatSession(
    val contextText: String,
    val messages: List<ChatMessage>,
)
