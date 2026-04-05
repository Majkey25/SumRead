package com.sumread.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.sumread.MainActivity
import com.sumread.domain.model.CaptureMode
import com.sumread.presentation.chat.ChatActivity
import com.sumread.presentation.capture.RegionSelectionActivity
import com.sumread.presentation.overlay.CapturePermissionActivity
import com.sumread.service.FloatingService
import com.sumread.service.MediaProjectionForegroundService

object IntentFactory {

    fun mainActivityPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            10,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    fun overlayService(context: Context): Intent {
        return Intent(context, FloatingService::class.java)
    }

    fun stopOverlayService(context: Context): Intent {
        return overlayService(context).apply {
            action = AppConfig.overlayServiceStopAction
        }
    }

    fun capturePermission(context: Context, mode: CaptureMode): Intent {
        return Intent(context, CapturePermissionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(AppConfig.capturePermissionModeKey, mode.name)
        }
    }

    fun mediaProjectionService(
        context: Context,
        mode: CaptureMode,
        resultCode: Int,
        resultData: Intent,
    ): Intent {
        return Intent(context, MediaProjectionForegroundService::class.java).apply {
            putExtra(AppConfig.capturePermissionModeKey, mode.name)
            putExtra(AppConfig.mediaProjectionResultCodeKey, resultCode)
            putExtra(AppConfig.mediaProjectionDataKey, resultData)
        }
    }

    fun regionSelection(
        context: Context,
        mode: CaptureMode,
        imagePath: String,
    ): Intent {
        return Intent(context, RegionSelectionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(AppConfig.capturePermissionModeKey, mode.name)
            putExtra(AppConfig.captureImagePathKey, imagePath)
        }
    }

    fun chat(context: Context): Intent {
        return Intent(context, ChatActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
