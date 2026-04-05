package com.sumread.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumread.domain.model.ChatSession
import com.sumread.domain.model.toFailureMessage
import com.sumread.domain.repository.ChatSessionRepository
import com.sumread.domain.repository.SettingsRepository
import com.sumread.domain.repository.SpeechRepository
import com.sumread.domain.usecase.SubmitChatMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatSessionRepository: ChatSessionRepository,
    private val submitChatMessageUseCase: SubmitChatMessageUseCase,
    private val settingsRepository: SettingsRepository,
    private val speechRepository: SpeechRepository,
) : ViewModel() {

    private val draftState = MutableStateFlow("")
    private val sendingState = MutableStateFlow(false)
    private val errorState = MutableStateFlow<String?>(null)

    val uiState = combine(
        chatSessionRepository.session,
        settingsRepository.settings,
        draftState,
        sendingState,
        errorState,
    ) { session, settings, draftMessage, isSending, errorMessage ->
        createUiState(
            session = session,
            languageTag = settings.languageTag,
            draftMessage = draftMessage,
            isSending = isSending,
            errorMessage = errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = createUiState(
            session = null,
            languageTag = "system",
            draftMessage = "",
            isSending = false,
            errorMessage = null,
        ),
    )

    fun updateDraftMessage(value: String) {
        draftState.value = value
    }

    fun sendMessage() {
        val message = draftState.value
        if (message.isBlank()) {
            return
        }
        viewModelScope.launch {
            sendingState.value = true
            errorState.value = null
            submitChatMessageUseCase(message)
                .onSuccess {
                    draftState.value = ""
                }
                .onFailure { error ->
                    errorState.value = error.toFailureMessage()
                }
            sendingState.value = false
        }
    }

    fun applySpeechResult(transcript: String) {
        draftState.value = transcript
    }

    fun speakMessage(text: String) {
        viewModelScope.launch {
            speechRepository.speak(text)
                .onFailure { error ->
                    errorState.value = error.toFailureMessage()
                }
        }
    }

    fun clearSession() {
        chatSessionRepository.clear()
    }

    fun clearError() {
        errorState.value = null
    }

    fun reportError(message: String) {
        errorState.value = message
    }

    private fun createUiState(
        session: ChatSession?,
        languageTag: String,
        draftMessage: String,
        isSending: Boolean,
        errorMessage: String?,
    ): ChatUiState {
        return ChatUiState(
            contextText = session?.contextText.orEmpty(),
            messages = session?.messages.orEmpty(),
            languageTag = languageTag,
            draftMessage = draftMessage,
            isSending = isSending,
            errorMessage = errorMessage,
            hasSession = session != null,
        )
    }
}
