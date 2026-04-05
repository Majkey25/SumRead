package com.sumread.presentation.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.sumread.domain.model.ChatMessage
import com.sumread.domain.model.ChatRole
import com.sumread.presentation.common.SumReadTopBar

@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onDraftChanged: (String) -> Unit,
    onSend: () -> Unit,
    onStartSpeechInput: () -> Unit,
    onSpeakMessage: (String) -> Unit,
    onClose: () -> Unit,
    onDismissError: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SumReadTopBar(
                title = "Ask AI",
                subtitle = "This session stays in memory only and uses the current capture as context.",
            )

            if (!uiState.hasSession) {
                Text(
                    text = "Start chat from a capture first.",
                    color = MaterialTheme.colorScheme.error,
                )
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Close")
                }
            } else {
                ContextCard(contextText = uiState.contextText)
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
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.messages, key = ChatMessage::id) { message ->
                        MessageCard(
                            message = message,
                            onSpeak = { onSpeakMessage(message.text) },
                        )
                    }
                }
                if (uiState.isSending) {
                    CircularProgressIndicator()
                }
                OutlinedTextField(
                    value = uiState.draftMessage,
                    onValueChange = onDraftChanged,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                    label = {
                        Text(text = "Ask about the captured content")
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextButton(
                        onClick = onStartSpeechInput,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = "Use microphone")
                    }
                    Button(
                        onClick = onSend,
                        enabled = !uiState.isSending && uiState.draftMessage.isNotBlank(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = "Send")
                    }
                }
                TextButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Close chat")
                }
            }
        }
    }
}

@Composable
private fun ContextCard(contextText: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Current screen context",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = contextText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 6,
            )
        }
    }
}

@Composable
private fun MessageCard(
    message: ChatMessage,
    onSpeak: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (message.role == ChatRole.USER) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (message.role == ChatRole.USER) "You" else "Assistant",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (message.role == ChatRole.ASSISTANT) {
                TextButton(onClick = onSpeak) {
                    Text(text = "Speak")
                }
            }
        }
    }
}
