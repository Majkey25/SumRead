package com.sumread.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.Button
import androidx.core.content.getSystemService
import com.sumread.R
import com.sumread.domain.model.CaptureMode
import com.sumread.util.AppConfig
import com.sumread.util.IntentFactory
import com.sumread.util.NotificationFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs

@AndroidEntryPoint
class FloatingService : Service() {

    @Inject
    lateinit var notificationFactory: NotificationFactory

    private var windowManager: WindowManager? = null
    private var bubbleView: View? = null
    private var actionsView: View? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var actionsParams: WindowManager.LayoutParams? = null

    override fun onCreate() {
        super.onCreate()
        runningStateInternal.value = true
        notificationFactory.ensureChannels()
        val notification = notificationFactory.overlayNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(AppConfig.overlayNotificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(AppConfig.overlayNotificationId, notification)
        }
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        val manager = getSystemService<WindowManager>()
        if (manager == null) {
            stopSelf()
            return
        }
        windowManager = manager
        addBubble()
        addActionsPanel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == AppConfig.overlayServiceStopAction) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        removeOverlayViews()
        runningStateInternal.value = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun addBubble() {
        val bubbleLayout = LayoutInflater.from(this).inflate(R.layout.overlay_bubble, null)
        val params = baseWindowParams().apply {
            gravity = Gravity.TOP or Gravity.START
            x = AppConfig.overlayStartX
            y = AppConfig.overlayStartY
        }
        bubbleLayout.setOnTouchListener(BubbleTouchListener(params))
        bubbleView = bubbleLayout
        bubbleParams = params
        windowManager?.addView(bubbleLayout, params)
    }

    private fun addActionsPanel() {
        val panel = LayoutInflater.from(this).inflate(R.layout.overlay_actions, null)
        panel.visibility = View.GONE
        panel.findViewById<Button>(R.id.actionRead).setOnClickListener {
            launchCapture(CaptureMode.READ_ALOUD)
        }
        panel.findViewById<Button>(R.id.actionSummary).setOnClickListener {
            launchCapture(CaptureMode.AI_SUMMARY)
        }
        panel.findViewById<Button>(R.id.actionChat).setOnClickListener {
            launchCapture(CaptureMode.AI_CHAT)
        }
        panel.findViewById<Button>(R.id.actionClipboard).setOnClickListener {
            actionsView?.visibility = View.GONE
            startActivity(IntentFactory.clipboardMode(this))
        }
        panel.findViewById<Button>(R.id.actionStop).setOnClickListener {
            stopSelf()
        }
        val params = baseWindowParams().apply {
            gravity = Gravity.TOP or Gravity.START
            x = AppConfig.overlayStartX + AppConfig.actionPanelOffsetX
            y = AppConfig.overlayStartY
        }
        actionsView = panel
        actionsParams = params
        windowManager?.addView(panel, params)
    }

    private fun launchCapture(mode: CaptureMode) {
        actionsView?.visibility = View.GONE
        startActivity(IntentFactory.capturePermission(this, mode))
    }

    private fun toggleActions() {
        val panel = actionsView ?: return
        panel.visibility = if (panel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        syncActionsPosition()
    }

    private fun syncActionsPosition() {
        val bubble = bubbleParams ?: return
        val panel = actionsParams ?: return
        panel.x = bubble.x + AppConfig.actionPanelOffsetX
        panel.y = bubble.y
        actionsView?.let {
            windowManager?.updateViewLayout(it, panel)
        }
    }

    private fun removeOverlayViews() {
        val manager = windowManager
        if (manager != null) {
            actionsView?.let(manager::removeViewImmediate)
            bubbleView?.let(manager::removeViewImmediate)
        }
        actionsView = null
        bubbleView = null
    }

    private fun baseWindowParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        )
    }

    private inner class BubbleTouchListener(
        private val params: WindowManager.LayoutParams,
    ) : View.OnTouchListener {
        private val touchSlop =
            ViewConfiguration.get(this@FloatingService).scaledTouchSlop * AppConfig.overlayTouchSlopScale
        private var startX = 0
        private var startY = 0
        private var initialX = 0
        private var initialY = 0
        private var dragging = false

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX.toInt()
                    startY = event.rawY.toInt()
                    initialX = params.x
                    initialY = params.y
                    dragging = false
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX.toInt() - startX
                    val deltaY = event.rawY.toInt() - startY
                    if (!dragging && (abs(deltaX) > touchSlop || abs(deltaY) > touchSlop)) {
                        dragging = true
                    }
                    if (dragging) {
                        params.x = initialX + deltaX
                        params.y = initialY + deltaY
                        windowManager?.updateViewLayout(view, params)
                        syncActionsPosition()
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (!dragging) {
                        view.performClick()
                        toggleActions()
                    }
                    return true
                }

                else -> return false
            }
        }
    }

    companion object {
        private val runningStateInternal = MutableStateFlow(false)
        val runningState: StateFlow<Boolean> = runningStateInternal
    }
}
