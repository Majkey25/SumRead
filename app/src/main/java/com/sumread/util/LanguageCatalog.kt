package com.sumread.util

data class LanguageOption(
    val languageTag: String,
    val title: String,
)

object LanguageCatalog {
    val supportedOptions: List<LanguageOption> = listOf(
        LanguageOption(languageTag = "system", title = "System default"),
        LanguageOption(languageTag = "en-US", title = "English"),
        LanguageOption(languageTag = "cs-CZ", title = "Czech"),
    )
}
