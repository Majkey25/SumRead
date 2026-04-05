package com.sumread.data.local

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sumread.domain.model.AiProviderType
import com.sumread.domain.model.AppSettings
import com.sumread.util.AppConfig
import com.sumread.util.LanguageCatalog

object SettingsSerializer {
    val providerKey = stringPreferencesKey("selected_provider")
    val speechRateKey = floatPreferencesKey("speech_rate")
    val speechPitchKey = floatPreferencesKey("speech_pitch")
    val languageTagKey = stringPreferencesKey("language_tag")

    fun fromPreferences(preferences: Preferences): AppSettings {
        return AppSettings(
            selectedProvider = AiProviderType.fromStorageKey(
                value = preferences[providerKey] ?: AiProviderType.GROQ.storageKey,
            ),
            speechRate = (preferences[speechRateKey] ?: AppConfig.defaultSpeechRate)
                .coerceIn(AppConfig.minSpeechRate, AppConfig.maxSpeechRate),
            speechPitch = (preferences[speechPitchKey] ?: AppConfig.defaultSpeechPitch)
                .coerceIn(AppConfig.minSpeechPitch, AppConfig.maxSpeechPitch),
            languageTag = preferences[languageTagKey] ?: LanguageCatalog.supportedOptions.first().languageTag,
        )
    }
}
