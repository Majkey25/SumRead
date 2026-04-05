package com.sumread.presentation.capture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sumread.domain.model.CaptureMode
import com.sumread.domain.model.CaptureSelection
import com.sumread.presentation.common.SumReadTheme
import com.sumread.util.AppConfig
import com.sumread.util.IntentFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegionSelectionActivity : ComponentActivity() {

    private val viewModel: SelectionResultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imagePath = intent.getStringExtra(AppConfig.captureImagePathKey).orEmpty()
        val mode = CaptureMode.fromName(
            intent.getStringExtra(AppConfig.capturePermissionModeKey) ?: CaptureMode.READ_ALOUD.name,
        )
        viewModel.loadCapture(imagePath)
        setContent {
            SumReadTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current
                var selection by remember { mutableStateOf<CaptureSelection?>(null) }

                LaunchedEffect(uiState.openChat) {
                    if (uiState.openChat) {
                        startActivity(IntentFactory.chat(context))
                        viewModel.consumeOpenChat()
                        finish()
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = mode.title,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            text = "Drag to select the exact region to process. The screenshot is kept only until this action finishes.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (uiState.isLoading) {
                            CircularProgressIndicator()
                        } else {
                            AndroidView(
                                factory = { viewContext ->
                                    RegionSelectionView(viewContext).apply {
                                        onSelectionChanged = { updatedSelection ->
                                            selection = updatedSelection
                                        }
                                    }
                                },
                                update = { view ->
                                    view.setBitmap(uiState.bitmap)
                                    view.onSelectionChanged = { updatedSelection ->
                                        selection = updatedSelection
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            )
                        }
                        uiState.resultText?.let { resultText ->
                            Text(
                                text = resultText,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                            )
                        }
                        uiState.errorMessage?.let { errorMessage ->
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        if (uiState.isProcessing) {
                            CircularProgressIndicator()
                        }
                        Button(
                            onClick = {
                                if (uiState.resultText != null && mode != CaptureMode.AI_CHAT) {
                                    finish()
                                } else {
                                    viewModel.processSelection(
                                        path = imagePath,
                                        mode = mode,
                                        selection = selection,
                                    )
                                }
                            },
                            enabled = !uiState.isProcessing && (selection != null || uiState.resultText != null),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = when {
                                    uiState.resultText != null && mode != CaptureMode.AI_CHAT -> "Done"
                                    mode == CaptureMode.AI_CHAT -> "Continue to chat"
                                    else -> "Process selection"
                                },
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.deleteCapture(imagePath)
                                finish()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = "Cancel")
                        }
                    }
                }
            }
        }
    }
}
