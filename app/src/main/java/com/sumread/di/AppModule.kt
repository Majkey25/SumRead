package com.sumread.di

import com.sumread.data.local.AndroidKeystoreSecretStore
import com.sumread.data.local.SecretStore
import com.sumread.data.repository.AiRepositoryImpl
import com.sumread.data.repository.ChatSessionRepositoryImpl
import com.sumread.data.repository.OcrRepositoryImpl
import com.sumread.data.repository.OverlayControllerImpl
import com.sumread.data.repository.SettingsRepositoryImpl
import com.sumread.data.repository.SpeechRepositoryImpl
import com.sumread.domain.repository.AiRepository
import com.sumread.domain.repository.ChatSessionRepository
import com.sumread.domain.repository.OcrRepository
import com.sumread.domain.repository.OverlayController
import com.sumread.domain.repository.SettingsRepository
import com.sumread.domain.repository.SpeechRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        implementation: SettingsRepositoryImpl,
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(
        implementation: AiRepositoryImpl,
    ): AiRepository

    @Binds
    @Singleton
    abstract fun bindChatSessionRepository(
        implementation: ChatSessionRepositoryImpl,
    ): ChatSessionRepository

    @Binds
    @Singleton
    abstract fun bindOcrRepository(
        implementation: OcrRepositoryImpl,
    ): OcrRepository

    @Binds
    @Singleton
    abstract fun bindOverlayController(
        implementation: OverlayControllerImpl,
    ): OverlayController

    @Binds
    @Singleton
    abstract fun bindSpeechRepository(
        implementation: SpeechRepositoryImpl,
    ): SpeechRepository

    @Binds
    @Singleton
    abstract fun bindSecretStore(
        implementation: AndroidKeystoreSecretStore,
    ): SecretStore
}
