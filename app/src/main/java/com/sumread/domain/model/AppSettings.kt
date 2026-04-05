package com.sumread.domain.model

data class AppSettings(
    val selectedProvider: AiProviderType,
    val speechRate: Float,
    val speechPitch: Float,
    val languageTag: String,
)
