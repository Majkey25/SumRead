package com.sumread.data.remote

data class OpenAiChatRequest(
    val model: String,
    val temperature: Float,
    val messages: List<OpenAiMessage>,
)

data class OpenAiMessage(
    val role: String,
    val content: String,
)

data class OpenAiChatResponse(
    val choices: List<OpenAiChoice>,
)

data class OpenAiChoice(
    val message: OpenAiMessage,
)