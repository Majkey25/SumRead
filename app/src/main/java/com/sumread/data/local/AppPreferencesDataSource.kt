package com.sumread.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.sumread.domain.model.AppSettings
import com.sumread.util.AppConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = AppConfig.preferencesName)

@Singleton
class AppPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map(SettingsSerializer::fromPreferences)

    suspend fun update(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[SettingsSerializer.providerKey] = settings.selectedProvider.storageKey
            preferences[SettingsSerializer.groqModelKey] = settings.groqModel
            preferences[SettingsSerializer.geminiModelKey] = settings.geminiModel
            preferences[SettingsSerializer.openaiModelKey] = settings.openaiModel
            preferences[SettingsSerializer.speechRateKey] = settings.speechRate
            preferences[SettingsSerializer.speechPitchKey] = settings.speechPitch
            preferences[SettingsSerializer.languageTagKey] = settings.languageTag
        }
    }
}
