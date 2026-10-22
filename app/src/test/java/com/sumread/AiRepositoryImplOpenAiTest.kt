package com.sumread

import com.google.common.truth.Truth.assertThat
import com.sumread.data.remote.GeminiAiProvider
import com.sumread.data.remote.GroqAiProvider
import com.sumread.data.remote.OpenAiAiProvider
import com.sumread.data.remote.OpenAiApiService
import com.sumread.data.remote.OpenAiChatResponse
import com.sumread.data.repository.AiRepositoryImpl
import com.sumread.domain.model.AiProviderType
import com.sumread.domain.model.AppSettings
import com.sumread.domain.model.ChatMessage
import com.sumread.domain.model.ChatRole
import com.sumread.domain.model.OperationException
import com.sumread.domain.model.OperationFailure
import com.sumread.domain.repository.SettingsRepository
import com.sumread.util.TestLoggerSetup
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AiRepositoryImplOpenAiTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUp() {
            TestLoggerSetup.setUpTestLogging()
        }
    }

    @Test
    fun `selected openai provider is used for summaries`() = runTest {
        val openAiCalls = AtomicInteger(0)
        val openAiService = object : OpenAiApiService {
            override suspend fun createChatCompletion(
                authorization: String,
                request: com.sumread.data.remote.OpenAiChatRequest,
            ): OpenAiChatResponse {
                openAiCalls.incrementAndGet()
                return OpenAiChatResponse(
                    choices = listOf(
                        com.sumread.data.remote.OpenAiChoice(
                            message = com.sumread.data.remote.OpenAiMessage(role = "assistant", content = "Summary"),
                        ),
                    ),
                )
            }
        }
        val repository = AiRepositoryImpl(
            settingsRepository = FakeSettingsRepository(
                initialSettings = AppSettings(
                    selectedProvider = AiProviderType.OPENAI,
                    groqModel = "groq-model",
                    geminiModel = "gemini-model",
                    openaiModel = "openai-model",
                    speechRate = 1.0f,
                    speechPitch = 1.0f,
                    languageTag = "system",
                ),
                apiKeys = mapOf(AiProviderType.OPENAI to "openai-key"),
            ),
            groqAiProvider = GroqAiProvider(FailingGroqService()),
            geminiAiProvider = GeminiAiProvider(FailingGeminiService()),
            openAiAiProvider = OpenAiAiProvider(openAiService),
        )

        val result = repository.summarize("Captured text")

        assertThat(result.getOrNull()).isEqualTo("Summary")
        assertThat(openAiCalls.get()).isEqualTo(1)
    }

    @Test
    fun `missing selected key never sends summary or chat to another configured provider`() = runTest {
        for (selected in AiProviderType.entries) {
            for (missingKey in listOf(null, "  ")) {
                val settings = FakeSettingsRepository(
                    AppSettings(selected, "groq-model", "gemini-model", "openai-model", 1f, 1f, "system"),
                    AiProviderType.entries.associateWith { "synthetic-key" }.toMutableMap().apply {
                        if (missingKey == null) remove(selected) else put(selected, missingKey)
                    },
                )
                val repository = AiRepositoryImpl(
                    settings,
                    GroqAiProvider(FailingGroqService()),
                    GeminiAiProvider(FailingGeminiService()),
                    OpenAiAiProvider(object : OpenAiApiService {
                        override suspend fun createChatCompletion(
                            authorization: String,
                            request: com.sumread.data.remote.OpenAiChatRequest,
                        ): OpenAiChatResponse = error("No provider should receive this request")
                    }),
                )

                val summary = repository.summarize("Synthetic private capture")
                val reply = repository.reply("Synthetic context", emptyList(), "Synthetic question")

                assertThat((summary.exceptionOrNull() as? OperationException)?.failure)
                    .isEqualTo(OperationFailure.MissingApiKey)
                assertThat((reply.exceptionOrNull() as? OperationException)?.failure)
                    .isEqualTo(OperationFailure.MissingApiKey)
                assertThat(settings.requestedProviders).containsExactly(selected, selected)
            }
        }
    }
}

private class FakeSettingsRepository(
    initialSettings: AppSettings,
    private val apiKeys: Map<AiProviderType, String>,
) : SettingsRepository {

    private val state = MutableStateFlow(initialSettings)
    val requestedProviders = mutableListOf<AiProviderType>()

    override val settings: Flow<AppSettings> = state

    override suspend fun updateSettings(settings: AppSettings) {
        state.value = settings
    }

    override suspend fun saveApiKey(provider: AiProviderType, value: String) = Unit

    override suspend fun clearApiKey(provider: AiProviderType) = Unit

    override suspend fun getApiKey(provider: AiProviderType): String? {
        requestedProviders += provider
        return apiKeys[provider]
    }

    override suspend fun hasApiKey(provider: AiProviderType): Boolean = apiKeys.containsKey(provider)
}

private class FailingGroqService : com.sumread.data.remote.GroqApiService {
    override suspend fun createChatCompletion(
        authorization: String,
        request: com.sumread.data.remote.GroqChatRequest,
    ): com.sumread.data.remote.GroqChatResponse {
        error("Groq should not be called when OpenAI is selected")
    }
}

private class FailingGeminiService : com.sumread.data.remote.GeminiApiService {
    override suspend fun generateContent(
        model: String,
        apiKey: String,
        request: com.sumread.data.remote.GeminiGenerateContentRequest,
    ): com.sumread.data.remote.GeminiGenerateContentResponse {
        error("Gemini should not be called when OpenAI is selected")
    }
}
