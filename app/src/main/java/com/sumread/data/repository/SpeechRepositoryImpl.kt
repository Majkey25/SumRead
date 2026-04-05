package com.sumread.data.repository

import com.sumread.data.local.TtsEngine
import com.sumread.domain.model.SpeechOptions
import com.sumread.domain.repository.SettingsRepository
import com.sumread.domain.repository.SpeechRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class SpeechRepositoryImpl @Inject constructor(
    private val ttsEngine: TtsEngine,
    private val settingsRepository: SettingsRepository,
) : SpeechRepository {

    override suspend fun speak(text: String): Result<Unit> {
        return runCatching {
            val settings = settingsRepository.settings.first()
            ttsEngine.speak(
                text = text,
                options = SpeechOptions(
                    rate = settings.speechRate,
                    pitch = settings.speechPitch,
                    languageTag = settings.languageTag,
                ),
            )
        }
    }
}
