package com.example.androidconcepts.model

data class CountryInfo(
    val id: Int? = null,
    val countryISOCode: String,
    val countryName: String?,
    val countryCurrency: String?,
    val countryCapital: String?,
    val countryFlagImageUrl: String?
)

data class CountryInfoResponse(
    val countryList: List<CountryInfo> // List of Currency objects
)
