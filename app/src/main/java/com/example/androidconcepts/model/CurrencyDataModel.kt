package com.example.androidconcepts.model

data class Currency(
    val isoCode: String,  // Example: "USD"
    val name: String      // Example: "US Dollar"
)

data class CurrencyListResponse(
    val currencyList: List<Currency> // List of Currency objects
)

