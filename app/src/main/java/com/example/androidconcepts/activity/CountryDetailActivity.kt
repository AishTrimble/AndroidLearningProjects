package com.example.androidconcepts.activity

import CountryRepository
import DBHelper
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.androidconcepts.R
import com.example.androidconcepts.data.CountryCapitalData
import com.example.androidconcepts.data.CountryCurrencyData
import com.example.androidconcepts.data.CountryFlagImgData
import com.example.androidconcepts.model.CountryInfo
import kotlinx.coroutines.launch


class CountryDetailActivity : AppCompatActivity() {
    private var response1: String? = null
    private var response2: String? = null
    private var response3: String? = null

    private lateinit var capitaltextView: TextView
    private lateinit var currencytextView: TextView
    private lateinit var flagtextView: TextView

    private lateinit var dbHelper: DBHelper
    private lateinit var countryRepository: CountryRepository
    private var isoCode: String? = null
    private var countryName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.countrydetail)
        capitaltextView = findViewById(R.id.textView2)
        currencytextView = findViewById(R.id.textView3)
        flagtextView = findViewById(R.id.textView4)
        dbHelper = DBHelper(this)
        countryRepository = CountryRepository(dbHelper)
        countryName = intent.getStringExtra("COUNTRY_NAME")
        isoCode = intent.getStringExtra("ISO_CODE")
        if (countryName != null && isoCode != null) {
            countryDataSource(isoCode!!)

        }
    }

    private fun countryDataSource(isoCode: String) {
        val exist: CountryInfo? = countryRepository.searchCountries(isoCode)
        if (exist != null) {
            Log.d(
                "Database",
                "Found Country: ${exist.countryName}, Capital: ${exist.countryCapital}"
            )
            lifecycleScope.launch {
                capitaltextView.text = exist.countryCapital
                currencytextView.text = exist.countryCurrency
                flagtextView.text = exist.countryFlagImageUrl

            }
        } else {
            fetchAndDisplayCapital(isoCode)
            fetchAndDisplayCurrency(isoCode)
            fetchAndDisplayFlag(isoCode)
        }
    }

    private fun fetchAndDisplayCapital(countryCode: String) {
        lifecycleScope.launch {
            response2 = CountryCapitalData.fetchCountryCapital(countryCode)
            capitaltextView.text = response2
            checkAndSaveData()

        }
    }

    private fun fetchAndDisplayCurrency(countryCode: String) {
        lifecycleScope.launch {
            response1 = CountryCurrencyData.fetchCountryCurrency(countryCode)
            currencytextView.text = response1
            checkAndSaveData()

        }
    }

    private fun fetchAndDisplayFlag(countryCode: String) {
        lifecycleScope.launch {
            response3 = CountryFlagImgData.fetchCountryFlag(countryCode)
            flagtextView.text = response3
            checkAndSaveData()

        }
    }

    private fun checkAndSaveData() {
        if (response1 != null && response2 != null && response3 != null) {
            Log.d("Database", " response")
            val countryInfo = CountryInfo(
                countryISOCode = intent.getStringExtra("ISO_CODE") ?: "UNKNOWN",
                countryName = intent.getStringExtra("COUNTRY_NAME")!!,
                countryCurrency = response1,
                countryCapital = response2,
                countryFlagImageUrl = response3
            )

            countryRepository.saveCountryInfo(countryInfo)
            val countryList = countryRepository.getAllCountries()
            for (country in countryList) {
                Log.d(
                    "Database",
                    "Country: ${country.countryName}, Capital: ${country.countryCapital}, FlagUrl : ${country.countryFlagImageUrl}"
                )
            }
        }
    }

}