package com.sumread.presentation.overlay

import android.app.Activity
import android.content.Context.MEDIA_PROJECTION_SERVICE
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sumread.domain.model.CaptureMode
import com.sumread.presentation.common.SumReadTheme
import com.sumread.util.AppConfig
import com.sumread.util.IntentFactory

class CapturePermissionActivity : ComponentActivity() {

    private val mode by lazy {
        CaptureMode.fromName(
            intent.getStringExtra(AppConfig.capturePermissionModeKey) ?: CaptureMode.READ_ALOUD.name,
        )
    }

    private val projectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                ContextCompat.startForegroundService(
                    this,
                    IntentFactory.mediaProjectionService(
                        context = this,
                        mode = mode,
                        resultCode = result.resultCode,
                        resultData = data,
                    ),
                )
            } else {
                Toast.makeText(this, "Screen capture permission was not granted.", Toast.LENGTH_SHORT).show()
            }
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SumReadTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CapturePermissionContent(
                        mode = mode,
                        onContinue = ::requestProjectionPermission,
                        onCancel = ::finish,
                    )
                }
            }
        }
    }

    private fun requestProjectionPermission() {
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projectionLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
    }
}

@Composable
private fun CapturePermissionContent(
    mode: CaptureMode,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = mode.title,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Android requires your explicit consent for every screen capture session. SumRead will capture one screenshot, let you choose a region, and process only that selected text.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "AI modes may send the OCR text from your selected region to the provider you configured in settings. Read aloud stays local.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Continue")
        }
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Cancel")
        }
    }
}
