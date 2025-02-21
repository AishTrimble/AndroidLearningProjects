package com.example.androidconcepts

data class Language(
    val isoCode: String,  // Example: "USD"
    val name: String      // Example: "US Dollar"
)

data class LanguageListResponse(
    val languageList: List<Language> // List of Currency objects
)
