package com.sumread

import com.google.common.truth.Truth.assertThat
import com.sumread.data.remote.OpenAiAiProvider
import com.sumread.data.remote.OpenAiApiService
import com.sumread.data.remote.OpenAiChatRequest
import com.sumread.data.remote.OpenAiChatResponse
import com.sumread.data.remote.OpenAiChoice
import com.sumread.data.remote.OpenAiMessage
import com.sumread.domain.model.ChatMessage
import com.sumread.domain.model.ChatRole
import com.sumread.domain.model.OperationException
import com.sumread.domain.model.OperationFailure
import com.sumread.util.AppConfig
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OpenAiAiProviderTest {

    @Test
    fun `summarize trims content and sends the expected request`() = runTest {
        val service = FakeOpenAiApiService(
            response = OpenAiChatResponse(
                choices = listOf(
                    OpenAiChoice(message = OpenAiMessage(role = "assistant", content = "  Short summary  ")),
                ),
            ),
        )
        val provider = OpenAiAiProvider(service)

        val result = provider.summarize(apiKey = "secret", model = AppConfig.openaiModel, sourceText = "Long source text")

        assertThat(result.getOrNull()).isEqualTo("Short summary")
        assertThat(service.lastAuthorization).isEqualTo("Bearer secret")
        assertThat(service.lastRequest?.model).isEqualTo(AppConfig.openaiModel)
        assertThat(service.lastRequest?.messages).hasSize(2)
        assertThat(service.lastRequest?.messages?.first()?.role).isEqualTo("system")
        assertThat(service.lastRequest?.messages?.last()?.content).contains("Long source text")
    }

    @Test
    fun `reply maps conversation into OpenAI messages`() = runTest {
        val service = FakeOpenAiApiService(
            response = OpenAiChatResponse(
                choices = listOf(
                    OpenAiChoice(message = OpenAiMessage(role = "assistant", content = "Answer")),
                ),
            ),
        )
        val provider = OpenAiAiProvider(service)

        val result = provider.reply(
            apiKey = "secret",
            model = AppConfig.openaiModel,
            contextText = "Invoice total is 42 EUR",
            conversation = listOf(
                ChatMessage(id = "1", role = ChatRole.USER, text = "What is the total?"),
                ChatMessage(id = "2", role = ChatRole.ASSISTANT, text = "It is listed on the page."),
            ),
            userMessage = "Answer in one sentence.",
        )

        assertThat(result.getOrNull()).isEqualTo("Answer")
        assertThat(service.lastRequest?.messages).hasSize(4)
        assertThat(service.lastRequest?.messages?.map { it.role }).containsExactly(
            "system",
            "user",
            "assistant",
            "user",
        ).inOrder()
        assertThat(service.lastRequest?.messages?.last()?.content).isEqualTo("Answer in one sentence.")
    }

    @Test
    fun `empty response becomes provider failure`() = runTest {
        val service = FakeOpenAiApiService(
            response = OpenAiChatResponse(choices = emptyList()),
        )
        val provider = OpenAiAiProvider(service)

        val result = provider.summarize(apiKey = "secret", model = AppConfig.openaiModel, sourceText = "Source")

        val error = result.exceptionOrNull()
        assertThat(error).isInstanceOf(OperationException::class.java)
        assertThat((error as OperationException).failure).isEqualTo(
            OperationFailure.ProviderFailure("OpenAI returned an empty response."),
        )
    }
}

private class FakeOpenAiApiService(
    private val response: OpenAiChatResponse,
) : OpenAiApiService {

    var lastAuthorization: String? = null
    var lastRequest: OpenAiChatRequest? = null

    override suspend fun createChatCompletion(
        authorization: String,
        request: OpenAiChatRequest,
    ): OpenAiChatResponse {
        lastAuthorization = authorization
        lastRequest = request
        return response
    }
}