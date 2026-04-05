package com.sumread.presentation.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.sumread.domain.model.PermissionSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionStatusResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun snapshot(): PermissionSnapshot {
        return PermissionSnapshot(
            overlayGranted = Settings.canDrawOverlays(context),
            microphoneGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
}
