package com.sumread

import androidx.datastore.preferences.core.mutablePreferencesOf
import com.google.common.truth.Truth.assertThat
import com.sumread.data.local.SettingsSerializer
import com.sumread.domain.model.AiProviderType
import org.junit.Test

class SettingsSerializerTest {

    @Test
    fun `empty preferences produce default settings`() {
        val settings = SettingsSerializer.fromPreferences(mutablePreferencesOf())

        assertThat(settings.selectedProvider).isEqualTo(AiProviderType.GROQ)
        assertThat(settings.languageTag).isEqualTo("system")
        assertThat(settings.speechRate).isEqualTo(1.0f)
        assertThat(settings.speechPitch).isEqualTo(1.0f)
    }

    @Test
    fun `stored values are mapped back into settings`() {
        val settings = SettingsSerializer.fromPreferences(
            mutablePreferencesOf(
                SettingsSerializer.providerKey to AiProviderType.GEMINI.storageKey,
                SettingsSerializer.languageTagKey to "cs-CZ",
                SettingsSerializer.speechRateKey to 1.3f,
                SettingsSerializer.speechPitchKey to 1.1f,
            ),
        )

        assertThat(settings.selectedProvider).isEqualTo(AiProviderType.GEMINI)
        assertThat(settings.languageTag).isEqualTo("cs-CZ")
        assertThat(settings.speechRate).isEqualTo(1.3f)
        assertThat(settings.speechPitch).isEqualTo(1.1f)
    }

    @Test
    fun `speech values are clamped into supported bounds`() {
        val settings = SettingsSerializer.fromPreferences(
            mutablePreferencesOf(
                SettingsSerializer.speechRateKey to 9.0f,
                SettingsSerializer.speechPitchKey to 0.1f,
            ),
        )

        assertThat(settings.speechRate).isEqualTo(1.6f)
        assertThat(settings.speechPitch).isEqualTo(0.7f)
    }
}
