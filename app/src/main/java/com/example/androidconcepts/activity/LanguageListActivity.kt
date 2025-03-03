package com.example.androidconcepts.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.androidconcepts.R
import com.example.androidconcepts.model.Language
import com.example.androidconcepts.model.LanguageListResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.StringReader


class LanguageListActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    //    private val countryMap =
//        HashMap<String, String>()
    private val displayList = mutableListOf<String>()
    private var languageListResponse: LanguageListResponse? = null  // Store parsed response

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.countrylist)

        listView = findViewById(R.id.listview)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
        listView.adapter = adapter

        sendSoapRequest()

        listView.setOnItemClickListener { _, _, position, _ ->
            languageListResponse?.let { response ->

                val selectedCountry = response.languageList[position]

                val intent = Intent(this, CountryDetailActivity::class.java)
                intent.putExtra("LANGUAGE_NAME", selectedCountry.name)
                intent.putExtra("LANGUAGE_ISO_CODE", selectedCountry.isoCode)
                startActivity(intent)
            } // Start new activity
        }
    }

    //
    private fun sendSoapRequest() {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()

            val soapXml = """<?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <ListOfLanguagesByName xmlns="http://www.oorsprong.org/websamples.countryinfo">
                </ListOfLanguagesByName>
              </soap:Body>
            </soap:Envelope>"""

            val mediaType = "text/xml; charset=utf-8".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, soapXml)

            val request = Request.Builder()
                .url("http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso")
                .post(body)
                .addHeader("Content-Type", "text/xml")
                .addHeader(
                    "SOAPAction",
                    "http://www.oorsprong.org/websamples.countryinfo/ListOfCountryNamesByName"
                )
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("SOAP Error", "Request failed: ${e.message}")
                    runOnUiThread {
                        Toast.makeText(
                            this@LanguageListActivity,
                            "Failed to fetch data",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string() ?: "Error: Empty Response"

                    val parsedData = parseCountryListXml(responseBody)
                    languageListResponse = parsedData
                    runOnUiThread {
//                        countryMap.clear()
//                        countryMap.putAll(parsedData) // Update global HashMap
                        languageListResponse?.let {

                            displayList.clear()
                            displayList.addAll(parsedData.languageList.map { "${it.name} (${it.isoCode})" })

                            adapter.notifyDataSetChanged()
                        }
                    }
                }

            })
        }
    }


    fun parseCountryListXml(xmlResponse: String): LanguageListResponse {
        val countryList = mutableListOf<Language>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlResponse))

            var eventType = parser.eventType
            var isoCode: String? = null
            var countryName: String? = null
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "sISOCode", "m:sISOCode" -> {
                            if (parser.next() == XmlPullParser.TEXT) isoCode = parser.text

                        }

                        "sName", "m:sName" -> {
                            if (parser.next() == XmlPullParser.TEXT) countryName = parser.text
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG && parser.name == "m:tLanguage") {
                    if (!isoCode.isNullOrEmpty() && !countryName.isNullOrEmpty()) {
//                        countryMap[isoCode] = countryName
                        countryList.add(Language(isoCode, countryName))

                        Log.d("Parsed_Country", "$countryName ($isoCode)") // Log parsed data
                    }
                    isoCode = null
                    countryName = null

                }
                eventType = parser.next()
            }

        } catch (e: Exception) {
            Log.e("XML Parsing", "Error parsing response: ${e.localizedMessage}")
        }
        return LanguageListResponse(countryList)
    }

}






