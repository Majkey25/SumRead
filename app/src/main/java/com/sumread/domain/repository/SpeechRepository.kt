package com.sumread.domain.repository

interface SpeechRepository {
    suspend fun speak(text: String): Result<Unit>
}
