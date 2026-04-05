package com.sumread.data.repository

import com.sumread.data.local.AppPreferencesDataSource
import com.sumread.data.local.SecretStore
import com.sumread.domain.model.AiProviderType
import com.sumread.domain.model.AppSettings
import com.sumread.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val appPreferencesDataSource: AppPreferencesDataSource,
    private val secretStore: SecretStore,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = appPreferencesDataSource.settings

    override suspend fun updateSettings(settings: AppSettings) {
        appPreferencesDataSource.update(settings)
    }

    override suspend fun saveApiKey(provider: AiProviderType, value: String) {
        secretStore.save(alias = provider.storageKey, value = value)
    }

    override suspend fun getApiKey(provider: AiProviderType): String? {
        return secretStore.read(alias = provider.storageKey)
    }

    override suspend fun hasApiKey(provider: AiProviderType): Boolean {
        return secretStore.contains(alias = provider.storageKey)
    }
}
