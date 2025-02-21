package com.example.androidconcepts

data class Country(
    val isoCode: String,  // Example: "USD"
    val name: String      // Example: "US Dollar"
)

data class CountryListResponse(
    val countryList: List<Country> // List of Currency objects
)

