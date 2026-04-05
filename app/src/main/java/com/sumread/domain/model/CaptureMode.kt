package com.sumread.domain.model

enum class CaptureMode(val title: String) {
    READ_ALOUD(title = "Read aloud"),
    AI_SUMMARY(title = "Summarize"),
    AI_CHAT(title = "Ask AI");

    companion object {
        fun fromName(value: String): CaptureMode {
            return entries.firstOrNull { it.name == value } ?: READ_ALOUD
        }
    }
}
