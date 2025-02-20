package com.example.androidconcepts

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CountryDetailActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.countrydetail)
        val countryName = intent.getStringExtra("COUNTRY_NAME")
        val isoCode = intent.getStringExtra("ISO_CODE")

        val textView: TextView = findViewById(R.id.textView2)
        textView.text = "Country: $countryName\nISO Code: $isoCode"
}}