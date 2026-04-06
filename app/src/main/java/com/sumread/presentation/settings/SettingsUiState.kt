package com.sumread.presentation.settings

import com.sumread.domain.model.AppSettings
import com.sumread.domain.model.PermissionSnapshot

data class SettingsUiState(
    val settings: AppSettings,
    val permissions: PermissionSnapshot,
    val isOverlayRunning: Boolean,
    val groqConfigured: Boolean,
    val geminiConfigured: Boolean,
    val openaiConfigured: Boolean,
    val transientMessage: String?,
)
