package com.sumread.presentation.capture

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumread.data.local.TemporaryCaptureStore
import com.sumread.domain.model.CaptureMode
import com.sumread.domain.model.CaptureSelection
import com.sumread.domain.model.toFailureMessage
import com.sumread.domain.repository.ChatSessionRepository
import com.sumread.domain.repository.OcrRepository
import com.sumread.domain.usecase.SpeakSelectionUseCase
import com.sumread.domain.usecase.SummarizeSelectionUseCase
import com.sumread.util.BitmapUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SelectionResultViewModel @Inject constructor(
    private val temporaryCaptureStore: TemporaryCaptureStore,
    private val ocrRepository: OcrRepository,
    private val speakSelectionUseCase: SpeakSelectionUseCase,
    private val summarizeSelectionUseCase: SummarizeSelectionUseCase,
    private val chatSessionRepository: ChatSessionRepository,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(SelectionResultUiState())
    val uiState: StateFlow<SelectionResultUiState> = mutableUiState.asStateFlow()

    fun loadCapture(path: String) {
        if (mutableUiState.value.bitmap != null || mutableUiState.value.isLoading) {
            return
        }
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            mutableUiState.value = mutableUiState.value.copy(isLoading = true, errorMessage = null)
            val bitmap = try {
                temporaryCaptureStore.load(path)
            } catch (_: IOException) {
                null
            }
            mutableUiState.value = mutableUiState.value.copy(
                bitmap = bitmap,
                isLoading = false,
                errorMessage = if (bitmap == null) "Unable to load the captured screenshot." else null,
            )
        }
    }

    fun processSelection(
        mode: CaptureMode,
        selection: CaptureSelection?,
    ) {
        val sourceBitmap = mutableUiState.value.bitmap
        if (sourceBitmap == null || selection == null) {
            mutableUiState.value = mutableUiState.value.copy(errorMessage = "Select a region before continuing.")
            return
        }

        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(isProcessing = true, errorMessage = null)
            runCatching {
                val croppedBitmap = BitmapUtils.crop(sourceBitmap, selection)
                val extractedText = ocrRepository.extractText(croppedBitmap).getOrThrow()
                when (mode) {
                    CaptureMode.READ_ALOUD -> {
                        val spokenText = speakSelectionUseCase(extractedText).getOrThrow()
                        mutableUiState.value = mutableUiState.value.copy(resultText = spokenText)
                    }

                    CaptureMode.AI_SUMMARY -> {
                        val summary = summarizeSelectionUseCase(extractedText).getOrThrow()
                        mutableUiState.value = mutableUiState.value.copy(resultText = summary)
                    }

                    CaptureMode.AI_CHAT -> {
                        chatSessionRepository.start(extractedText)
                        mutableUiState.value = mutableUiState.value.copy(openChat = true)
                    }

                    CaptureMode.CLIPBOARD -> {
                        mutableUiState.value = mutableUiState.value.copy(resultText = extractedText)
                    }
                }
            }.onFailure { error ->
                mutableUiState.value = mutableUiState.value.copy(errorMessage = error.toFailureMessage())
            }
            mutableUiState.value = mutableUiState.value.copy(isProcessing = false)
        }
    }

    fun consumeOpenChat() {
        mutableUiState.value = mutableUiState.value.copy(openChat = false)
    }

    fun clearError() {
        mutableUiState.value = mutableUiState.value.copy(errorMessage = null)
    }
}

data class SelectionResultUiState(
    val bitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val resultText: String? = null,
    val errorMessage: String? = null,
    val openChat: Boolean = false,
)
