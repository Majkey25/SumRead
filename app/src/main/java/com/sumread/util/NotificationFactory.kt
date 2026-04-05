package com.sumread.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.sumread.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun ensureChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        val overlayChannel = NotificationChannel(
            AppConfig.overlayChannelId,
            context.getString(R.string.overlay_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.overlay_channel_description)
        }
        val captureChannel = NotificationChannel(
            AppConfig.captureChannelId,
            context.getString(R.string.capture_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.capture_channel_description)
        }
        manager.createNotificationChannel(overlayChannel)
        manager.createNotificationChannel(captureChannel)
    }

    fun overlayNotification(): Notification {
        return NotificationCompat.Builder(context, AppConfig.overlayChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.overlay_notification_title))
            .setContentText(context.getString(R.string.overlay_notification_text))
            .setContentIntent(IntentFactory.mainActivityPendingIntent(context))
            .setOngoing(true)
            .build()
    }

    fun captureNotification(): Notification {
        return NotificationCompat.Builder(context, AppConfig.captureChannelId)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle(context.getString(R.string.capture_notification_title))
            .setContentText(context.getString(R.string.capture_notification_text))
            .setContentIntent(IntentFactory.mainActivityPendingIntent(context))
            .setOngoing(true)
            .build()
    }
}
