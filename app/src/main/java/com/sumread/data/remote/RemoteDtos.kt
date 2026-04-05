package com.sumread.data.remote

data class GroqChatRequest(
    val model: String,
    val temperature: Float,
    val messages: List<GroqMessage>,
)

data class GroqMessage(
    val role: String,
    val content: String,
)

data class GroqChatResponse(
    val choices: List<GroqChoice>,
)

data class GroqChoice(
    val message: GroqMessage,
)

data class GeminiGenerateContentRequest(
    val systemInstruction: GeminiContent?,
    val contents: List<GeminiContent>,
)

data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate>?,
)

data class GeminiCandidate(
    val content: GeminiContent?,
)

data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>,
)

data class GeminiPart(
    val text: String,
)
