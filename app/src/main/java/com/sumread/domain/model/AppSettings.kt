package com.sumread.domain.model

data class AppSettings(
    val selectedProvider: AiProviderType,
    val groqModel: String,
    val geminiModel: String,
    val openaiModel: String,
    val speechRate: Float,
    val speechPitch: Float,
    val languageTag: String,
)
