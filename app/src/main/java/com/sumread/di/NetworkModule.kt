package com.sumread.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sumread.data.remote.GeminiApiService
import com.sumread.data.remote.GroqApiService
import com.sumread.data.remote.OpenAiApiService
import com.sumread.util.AppConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .connectTimeout(AppConfig.networkTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(AppConfig.networkTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(AppConfig.networkTimeoutSeconds, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideGroqApiService(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): GroqApiService {
        return Retrofit.Builder()
            .baseUrl(AppConfig.groqBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GroqApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGeminiApiService(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): GeminiApiService {
        return Retrofit.Builder()
            .baseUrl(AppConfig.geminiBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GeminiApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenAiApiService(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): OpenAiApiService {
        return Retrofit.Builder()
            .baseUrl(AppConfig.openaiBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(OpenAiApiService::class.java)
    }
}
