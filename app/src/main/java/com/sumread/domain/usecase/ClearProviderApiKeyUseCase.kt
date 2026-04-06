package com.sumread.domain.usecase

import com.sumread.domain.model.AiProviderType
import com.sumread.domain.repository.SettingsRepository
import javax.inject.Inject

class ClearProviderApiKeyUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(provider: AiProviderType) {
        settingsRepository.clearApiKey(provider)
    }
}
