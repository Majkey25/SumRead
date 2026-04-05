package com.sumread

import com.google.common.truth.Truth.assertThat
import com.sumread.data.remote.AiPromptFactory
import com.sumread.domain.model.ChatMessage
import com.sumread.domain.model.ChatRole
import org.junit.Test

class AiPromptFactoryTest {

    @Test
    fun `summary user prompt includes the source text`() {
        val prompt = AiPromptFactory.summaryUserPrompt("Example text")

        assertThat(prompt).contains("Example text")
        assertThat(prompt).contains("Source text")
    }

    @Test
    fun `chat system prompt embeds capture context`() {
        val prompt = AiPromptFactory.chatSystemPrompt("Invoice total is 42 EUR")

        assertThat(prompt).contains("Invoice total is 42 EUR")
        assertThat(prompt).contains("captured screen content")
    }

    @Test
    fun `chat messages prepend system context and preserve roles`() {
        val messages = AiPromptFactory.chatMessages(
            contextText = "Paragraph from article",
            conversation = listOf(
                ChatMessage(id = "1", role = ChatRole.USER, text = "What is this about?"),
                ChatMessage(id = "2", role = ChatRole.ASSISTANT, text = "It is about OCR."),
            ),
            userMessage = "Summarize it again.",
        )

        assertThat(messages).hasSize(4)
        assertThat(messages.first().role).isEqualTo("system")
        assertThat(messages[1].role).isEqualTo("user")
        assertThat(messages[2].role).isEqualTo("assistant")
        assertThat(messages.last().content).isEqualTo("Summarize it again.")
    }
}
