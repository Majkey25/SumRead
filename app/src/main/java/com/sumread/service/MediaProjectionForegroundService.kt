package com.sumread.service

import android.app.Service
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager
import com.sumread.domain.model.CaptureMode
import com.sumread.util.AppConfig
import com.sumread.util.BitmapUtils
import com.sumread.util.IntentFactory
import com.sumread.util.NotificationFactory
import com.sumread.util.DispatchersProvider
import com.sumread.data.local.TemporaryCaptureStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

@AndroidEntryPoint
class MediaProjectionForegroundService : Service() {

    @Inject
    lateinit var notificationFactory: NotificationFactory

    @Inject
    lateinit var temporaryCaptureStore: TemporaryCaptureStore

    @Inject
    lateinit var dispatchersProvider: DispatchersProvider

    private lateinit var serviceScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        notificationFactory.ensureChannels()
        startForeground(AppConfig.captureNotificationId, notificationFactory.captureNotification())
        serviceScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra(AppConfig.mediaProjectionResultCodeKey, Int.MIN_VALUE)
            ?: Int.MIN_VALUE
        val resultData = intent?.getParcelableExtra<Intent>(AppConfig.mediaProjectionDataKey)
        val mode = CaptureMode.fromName(
            intent?.getStringExtra(AppConfig.capturePermissionModeKey) ?: CaptureMode.READ_ALOUD.name,
        )

        if (resultCode == Int.MIN_VALUE || resultData == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        serviceScope.launch {
            runCatching {
                handleCapture(mode = mode, resultCode = resultCode, resultData = resultData)
            }
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun handleCapture(
        mode: CaptureMode,
        resultCode: Int,
        resultData: Intent,
    ) {
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val projection = mediaProjectionManager.getMediaProjection(resultCode, resultData) ?: return
        val bitmap = captureBitmap(projection)
        val imagePath = temporaryCaptureStore.save(bitmap)
        startActivity(IntentFactory.regionSelection(this, mode, imagePath))
    }

    private suspend fun captureBitmap(projection: MediaProjection) = withContext(dispatchersProvider.main) {
        val displayMetrics = DisplayMetrics()
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)

        val imageReader = ImageReader.newInstance(
            displayMetrics.widthPixels,
            displayMetrics.heightPixels,
            android.graphics.PixelFormat.RGBA_8888,
            2,
        )
        val handlerThread = HandlerThread("media-projection-capture").apply { start() }
        val handler = Handler(handlerThread.looper)
        var virtualDisplay: VirtualDisplay? = null

        try {
            withTimeout(4_000) {
                suspendCancellableCoroutine { continuation ->
                    imageReader.setOnImageAvailableListener({ reader ->
                        val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                        image.use {
                            if (continuation.isActive) {
                                runCatching { BitmapUtils.fromImage(it) }
                                    .onSuccess(continuation::resume)
                                    .onFailure(continuation::resumeWithException)
                            }
                        }
                    }, handler)

                    virtualDisplay = projection.createVirtualDisplay(
                        "sumread_capture",
                        displayMetrics.widthPixels,
                        displayMetrics.heightPixels,
                        displayMetrics.densityDpi,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        imageReader.surface,
                        null,
                        handler,
                    )
                }
            }
        } finally {
            virtualDisplay?.release()
            imageReader.close()
            projection.stop()
            handlerThread.quitSafely()
        }
    }
}
