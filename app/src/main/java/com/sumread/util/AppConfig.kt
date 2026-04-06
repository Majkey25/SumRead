package com.sumread.util

object AppConfig {
    const val overlayChannelId = "sumread_overlay"
    const val captureChannelId = "sumread_capture"
    const val overlayNotificationId = 1001
    const val captureNotificationId = 1002
    const val overlayTouchSlopScale = 1
    const val overlayStartX = 32
    const val overlayStartY = 240
    const val actionPanelOffsetX = 76
    const val temporaryCaptureFolder = "captures"
    const val preferencesName = "sumread_settings"
    const val secretsName = "sumread_secrets"
    const val defaultSpeechRate = 1.0f
    const val defaultSpeechPitch = 1.0f
    const val minSpeechRate = 0.6f
    const val maxSpeechRate = 1.6f
    const val minSpeechPitch = 0.7f
    const val maxSpeechPitch = 1.4f
    const val groqBaseUrl = "https://api.groq.com/"
    const val groqModel = "llama-3.3-70b-versatile"
    val groqModels = listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant", "gemma2-9b-it", "mixtral-8x7b-32768")
    const val geminiBaseUrl = "https://generativelanguage.googleapis.com/"
    const val geminiModel = "gemini-2.5-flash"
    val geminiModels = listOf("gemini-2.5-flash", "gemini-2.0-flash", "gemini-1.5-flash")
    const val openaiBaseUrl = "https://api.openai.com/"
    const val openaiModel = "gpt-4.1-mini"
    val openaiModels = listOf("gpt-4.1-nano", "gpt-4.1-mini", "gpt-4.1", "gpt-4o-mini", "gpt-4o")
    const val networkTimeoutSeconds = 30L
    const val overlayServiceStopAction = "com.sumread.action.STOP_OVERLAY"
    const val capturePermissionModeKey = "capture_mode"
    const val mediaProjectionResultCodeKey = "media_projection_result_code"
    const val mediaProjectionDataKey = "media_projection_data"
    const val captureImagePathKey = "capture_image_path"
}
