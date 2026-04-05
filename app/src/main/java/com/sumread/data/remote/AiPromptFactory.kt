package com.sumread.data.remote

import com.sumread.domain.model.ChatMessage
import com.sumread.domain.model.ChatRole

object AiPromptFactory {

    fun summarySystemPrompt(): String {
        return """
            You rewrite OCR text for listening.
            Keep the meaning intact.
            Prefer a concise spoken summary when the source is long or repetitive.
            Prefer a cleaned readable version when the source is already short.
            Never mention that the text came from OCR.
        """.trimIndent()
    }

    fun summaryUserPrompt(sourceText: String): String {
        return "Source text:\n$sourceText"
    }

    fun chatSystemPrompt(contextText: String): String {
        return """
            You answer questions about the user's captured screen content.
            Use the capture context below as your primary source of truth.
            If the answer is not supported by the context, say that clearly.
            Keep answers direct and practical.

            Capture context:
            $contextText
        """.trimIndent()
    }

    fun chatMessages(
        contextText: String,
        conversation: List<ChatMessage>,
        userMessage: String,
    ): List<PromptMessage> {
        val messages = mutableListOf(
            PromptMessage(role = "system", content = chatSystemPrompt(contextText)),
        )
        conversation.forEach { message ->
            messages += PromptMessage(
                role = when (message.role) {
                    ChatRole.USER -> "user"
                    ChatRole.ASSISTANT -> "assistant"
                },
                content = message.text,
            )
        }
        messages += PromptMessage(role = "user", content = userMessage)
        return messages
    }
}

data class PromptMessage(
    val role: String,
    val content: String,
)
