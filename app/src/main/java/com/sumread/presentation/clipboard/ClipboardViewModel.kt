package com.sumread.presentation.clipboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumread.domain.model.toFailureMessage
import com.sumread.domain.repository.ChatSessionRepository
import com.sumread.domain.usecase.SpeakSelectionUseCase
import com.sumread.domain.usecase.SummarizeSelectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ClipboardViewModel @Inject constructor(
    private val speakSelectionUseCase: SpeakSelectionUseCase,
    private val summarizeSelectionUseCase: SummarizeSelectionUseCase,
    private val chatSessionRepository: ChatSessionRepository,
) : ViewModel() {

    private val state = MutableStateFlow(ClipboardUiState())
    val uiState: StateFlow<ClipboardUiState> = state.asStateFlow()

    fun loadClipboardText(text: String) {
        state.value = state.value.copy(
            clipboardText = text,
            isEmpty = text.isBlank(),
        )
    }

    fun readAloud() {
        val text = state.value.clipboardText.ifBlank { return }
        viewModelScope.launch {
            state.value = state.value.copy(isProcessing = true, errorMessage = null)
            speakSelectionUseCase(text)
                .onFailure { state.value = state.value.copy(errorMessage = it.toFailureMessage()) }
            state.value = state.value.copy(isProcessing = false)
        }
    }

    fun summarize() {
        val text = state.value.clipboardText.ifBlank { return }
        viewModelScope.launch {
            state.value = state.value.copy(isProcessing = true, errorMessage = null)
            summarizeSelectionUseCase(text)
                .onSuccess { summary ->
                    state.value = state.value.copy(resultText = summary)
                }
                .onFailure { state.value = state.value.copy(errorMessage = it.toFailureMessage()) }
            state.value = state.value.copy(isProcessing = false)
        }
    }

    fun startChat() {
        val text = state.value.clipboardText.ifBlank { return }
        chatSessionRepository.start(text)
        state.value = state.value.copy(openChat = true)
    }

    fun consumeOpenChat() {
        state.value = state.value.copy(openChat = false)
    }

    fun clearError() {
        state.value = state.value.copy(errorMessage = null)
    }
}

data class ClipboardUiState(
    val clipboardText: String = "",
    val isEmpty: Boolean = false,
    val isProcessing: Boolean = false,
    val resultText: String? = null,
    val errorMessage: String? = null,
    val openChat: Boolean = false,
)
