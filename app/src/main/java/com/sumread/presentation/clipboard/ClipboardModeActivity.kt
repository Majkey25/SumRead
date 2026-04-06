package com.sumread.presentation.clipboard

import android.content.ClipboardManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sumread.presentation.common.SumReadTheme
import com.sumread.util.IntentFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClipboardModeActivity : ComponentActivity() {

    private val viewModel: ClipboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val clipboardManager = getSystemService(ClipboardManager::class.java)
        val clipboardText = clipboardManager?.primaryClip
            ?.getItemAt(0)
            ?.coerceToText(this)
            ?.toString()
            ?.trim()
            .orEmpty()
        viewModel.loadClipboardText(clipboardText)

        setContent {
            SumReadTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(uiState.openChat) {
                    if (uiState.openChat) {
                        startActivity(IntentFactory.chat(this@ClipboardModeActivity))
                        viewModel.consumeOpenChat()
                        finish()
                    }
                }

                ClipboardModeScreen(
                    uiState = uiState,
                    onReadAloud = viewModel::readAloud,
                    onSummarize = viewModel::summarize,
                    onChat = viewModel::startChat,
                    onDismissError = viewModel::clearError,
                    onClose = ::finish,
                )
            }
        }
    }
}

@Composable
private fun ClipboardModeScreen(
    uiState: ClipboardUiState,
    onReadAloud: () -> Unit,
    onSummarize: () -> Unit,
    onChat: () -> Unit,
    onDismissError: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Clipboard", style = MaterialTheme.typography.headlineSmall)

            if (uiState.isEmpty) {
                Text(
                    text = "Clipboard is empty or contains no text.",
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Clipboard text",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = uiState.clipboardText,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 8,
                        )
                    }
                }

                uiState.resultText?.let { result ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Result",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = result,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }

                uiState.errorMessage?.let { message ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = message,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            TextButton(onClick = onDismissError) {
                                Text(text = "Dismiss")
                            }
                        }
                    }
                }

                if (uiState.isProcessing) {
                    CircularProgressIndicator()
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onReadAloud, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Read aloud")
                        }
                        Button(onClick = onSummarize, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Summarize with AI")
                        }
                        Button(onClick = onChat, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Ask AI about this text")
                        }
                    }
                }
            }

            TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Close")
            }
        }
    }
}
