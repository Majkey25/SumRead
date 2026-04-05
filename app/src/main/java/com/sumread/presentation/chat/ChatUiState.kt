package com.sumread.presentation.chat

import com.sumread.domain.model.ChatMessage

data class ChatUiState(
    val contextText: String,
    val messages: List<ChatMessage>,
    val languageTag: String,
    val draftMessage: String,
    val isSending: Boolean,
    val errorMessage: String?,
    val hasSession: Boolean,
)
