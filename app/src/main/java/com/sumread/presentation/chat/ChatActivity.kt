package com.sumread.presentation.chat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sumread.presentation.common.SumReadTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SumReadTheme {
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                val recognizer = remember {
                    ChatSpeechRecognizer(
                        context = this,
                        onResult = viewModel::applySpeechResult,
                        onError = viewModel::reportError,
                    )
                }

                DisposableEffect(Unit) {
                    onDispose {
                        recognizer.destroy()
                    }
                }

                ChatScreen(
                    uiState = uiState,
                    onDraftChanged = viewModel::updateDraftMessage,
                    onSend = viewModel::sendMessage,
                    onStartSpeechInput = {
                        if (
                            ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.RECORD_AUDIO,
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            recognizer.start(languageTag = uiState.languageTag)
                        } else {
                            viewModel.reportError("Microphone permission is required for speech input.")
                        }
                    },
                    onSpeakMessage = viewModel::speakMessage,
                    onClose = {
                        viewModel.clearSession()
                        finish()
                    },
                    onDismissError = viewModel::clearError,
                )
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing) {
            viewModel.clearSession()
        }
        super.onDestroy()
    }
}
