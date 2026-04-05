package com.sumread

import com.google.common.truth.Truth.assertThat
import com.sumread.data.repository.ChatSessionRepositoryImpl
import com.sumread.domain.model.ChatRole
import org.junit.Test

class ChatSessionRepositoryImplTest {

    @Test
    fun `start creates an empty in-memory session`() {
        val repository = ChatSessionRepositoryImpl()

        repository.start("Captured article text")

        assertThat(repository.session.value?.contextText).isEqualTo("Captured article text")
        assertThat(repository.session.value?.messages).isEmpty()
    }

    @Test
    fun `add user and assistant messages appends messages in order`() {
        val repository = ChatSessionRepositoryImpl()
        repository.start("Context")

        repository.addUserMessage("What is this?")
        repository.addAssistantMessage("This is a receipt.")

        val messages = repository.session.value?.messages.orEmpty()
        assertThat(messages).hasSize(2)
        assertThat(messages[0].role).isEqualTo(ChatRole.USER)
        assertThat(messages[1].role).isEqualTo(ChatRole.ASSISTANT)
    }

    @Test
    fun `clear removes the active session`() {
        val repository = ChatSessionRepositoryImpl()
        repository.start("Context")

        repository.clear()

        assertThat(repository.session.value).isNull()
    }
}
