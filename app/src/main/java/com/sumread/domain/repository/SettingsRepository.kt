package com.sumread.domain.repository

import com.sumread.domain.model.AiProviderType
import com.sumread.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun updateSettings(settings: AppSettings)
    suspend fun saveApiKey(provider: AiProviderType, value: String)
    suspend fun getApiKey(provider: AiProviderType): String?
    suspend fun hasApiKey(provider: AiProviderType): Boolean
}
