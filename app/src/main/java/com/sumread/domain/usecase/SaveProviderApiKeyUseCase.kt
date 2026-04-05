package com.sumread.domain.usecase

import com.sumread.domain.model.AiProviderType
import com.sumread.domain.repository.SettingsRepository
import javax.inject.Inject

class SaveProviderApiKeyUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(provider: AiProviderType, value: String) {
        settingsRepository.saveApiKey(provider = provider, value = value.trim())
    }
}
