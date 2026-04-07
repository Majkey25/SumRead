package com.sumread.util

data class LanguageOption(
    val languageTag: String,
    val title: String,
)

object LanguageCatalog {
    val supportedOptions: List<LanguageOption> = listOf(
        LanguageOption(languageTag = "system", title = "System default"),
        LanguageOption(languageTag = "en-US", title = "English (US)"),
        LanguageOption(languageTag = "en-GB", title = "English (UK)"),
        LanguageOption(languageTag = "cs-CZ", title = "Czech"),
        LanguageOption(languageTag = "de-DE", title = "German"),
        LanguageOption(languageTag = "fr-FR", title = "French"),
        LanguageOption(languageTag = "es-ES", title = "Spanish (Spain)"),
        LanguageOption(languageTag = "es-US", title = "Spanish (US)"),
        LanguageOption(languageTag = "it-IT", title = "Italian"),
        LanguageOption(languageTag = "pt-BR", title = "Portuguese (Brazil)"),
        LanguageOption(languageTag = "pt-PT", title = "Portuguese (Portugal)"),
        LanguageOption(languageTag = "nl-NL", title = "Dutch"),
        LanguageOption(languageTag = "pl-PL", title = "Polish"),
        LanguageOption(languageTag = "ru-RU", title = "Russian"),
        LanguageOption(languageTag = "sv-SE", title = "Swedish"),
        LanguageOption(languageTag = "nb-NO", title = "Norwegian"),
        LanguageOption(languageTag = "da-DK", title = "Danish"),
        LanguageOption(languageTag = "fi-FI", title = "Finnish"),
        LanguageOption(languageTag = "tr-TR", title = "Turkish"),
        LanguageOption(languageTag = "ar-SA", title = "Arabic"),
        LanguageOption(languageTag = "hi-IN", title = "Hindi"),
        LanguageOption(languageTag = "ja-JP", title = "Japanese"),
        LanguageOption(languageTag = "ko-KR", title = "Korean"),
        LanguageOption(languageTag = "zh-CN", title = "Chinese (Simplified)"),
        LanguageOption(languageTag = "zh-TW", title = "Chinese (Traditional)"),
        LanguageOption(languageTag = "vi-VN", title = "Vietnamese"),
        LanguageOption(languageTag = "th-TH", title = "Thai"),
        LanguageOption(languageTag = "uk-UA", title = "Ukrainian"),
    )
}
