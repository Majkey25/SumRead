package com.sumread.domain.model

enum class AiProviderType(val storageKey: String, val title: String) {
    GROQ(storageKey = "groq", title = "Groq"),
    GEMINI(storageKey = "gemini", title = "Gemini");

    companion object {
        fun fromStorageKey(value: String): AiProviderType {
            return entries.firstOrNull { it.storageKey == value } ?: GROQ
        }
    }
}
