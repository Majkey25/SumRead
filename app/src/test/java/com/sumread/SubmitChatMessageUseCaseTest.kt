package com.sumread

import com.google.common.truth.Truth.assertThat
import com.sumread.domain.model.ChatMessage
import com.sumread.domain.model.ChatRole
import com.sumread.domain.repository.AiRepository
import com.sumread.domain.usecase.SubmitChatMessageUseCase
import com.sumread.data.repository.ChatSessionRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SubmitChatMessageUseCaseTest {

    @Test
    fun `blank message returns a failure`() = runTest {
        val repository = ChatSessionRepositoryImpl().apply { start("Context") }
        val useCase = SubmitChatMessageUseCase(
            aiRepository = FakeAiRepository(),
            chatSessionRepository = repository,
        )

        val result = useCase("   ")

        assertThat(result.isFailure).isTrue()
        assertThat(repository.session.value?.messages).isEmpty()
    }

    @Test
    fun `missing session returns a failure`() = runTest {
        val useCase = SubmitChatMessageUseCase(
            aiRepository = FakeAiRepository(),
            chatSessionRepository = ChatSessionRepositoryImpl(),
        )

        val result = useCase("What is this?")

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `successful call appends user and assistant messages`() = runTest {
        val repository = ChatSessionRepositoryImpl().apply { start("Captured memo") }
        val useCase = SubmitChatMessageUseCase(
            aiRepository = FakeAiRepository(),
            chatSessionRepository = repository,
        )

        val result = useCase("Summarize it.")
        val messages = repository.session.value?.messages.orEmpty()

        assertThat(result.getOrNull()).isEqualTo("Assistant reply")
        assertThat(messages).hasSize(2)
        assertThat(messages[0].role).isEqualTo(ChatRole.USER)
        assertThat(messages[0].text).isEqualTo("Summarize it.")
        assertThat(messages[1].role).isEqualTo(ChatRole.ASSISTANT)
        assertThat(messages[1].text).isEqualTo("Assistant reply")
    }
}

private class FakeAiRepository : AiRepository {
    override suspend fun summarize(sourceText: String): Result<String> {
        return Result.success(sourceText)
    }

    override suspend fun reply(
        contextText: String,
        conversation: List<ChatMessage>,
        userMessage: String,
    ): Result<String> {
        return Result.success("Assistant reply")
    }
}
