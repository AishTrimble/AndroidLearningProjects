package com.example.androidconcepts
//package com.example.okhttp3
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.StringReader
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

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainactivity)

        val etCountryCode = findViewById<EditText>(R.id.etCountryCode)
        val btnSendRequest = findViewById<Button>(R.id.btnSendRequest)
        val tvResponse = findViewById<TextView>(R.id.tvResponse)
        val btnCountryList = findViewById<Button>(R.id.btnCountrylist)

        btnSendRequest.setOnClickListener {
            val countryCode = etCountryCode.text.toString().trim()
            if (countryCode.isNotEmpty()) {

                sendSoapRequest(countryCode) { response ->
                    val parsedResponse = parseXmlWithXmlPullParser(response)

                    runOnUiThread {
                        tvResponse.text = parsedResponse
                    }
                }
            } else {
                tvResponse.text = "Please enter a valid country code."
            }
        }

        btnCountryList.setOnClickListener {

                        val intent = Intent(this, CountryList::class.java)
                        startActivity(intent)
                    }
                }





        fun parseXmlWithXmlPullParser(xmlResponse: String): String {
            try {
                val factory = XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setFeature(
                    XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                    true
                ) // Enable namespaces
                parser.setInput(StringReader(xmlResponse)) // Correct input source

                var eventType = parser.eventType
                var isoCode: String? = null
                var CurrencyName: String? = null

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        when (parser.name) {
                            "sISOCode", "m:sISOCode" -> {
                                if (parser.next() == XmlPullParser.TEXT) isoCode = parser.text
                            }

                            "sName", "m:sName" -> {
                                if (parser.next() == XmlPullParser.TEXT) CurrencyName = parser.text
                            }
                        }}
//
                    eventType = parser.next()
                }

                return if (!isoCode.isNullOrEmpty() && !CurrencyName.isNullOrEmpty()) {
                    "ISO Code: $isoCode\nCurrency Name: $CurrencyName"
                } else {
                    "Error: Could not extract currency details"
                }
            } catch (e: Exception) {

                return "Error parsing response: ${e.localizedMessage}"
            }

        }



        private fun sendSoapRequest(countryCode: String,onResponse: (String) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val client = OkHttpClient()

            val soapXml = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <CountryCurrency xmlns="http://www.oorsprong.org/websamples.countryinfo">
                      <sCountryISOCode>$countryCode</sCountryISOCode>
                    </CountryCurrency>
                  </soap:Body>
                </soap:Envelope>
            """.trimIndent()


                val mediaType = "text/xml; charset=utf-8".toMediaTypeOrNull()
                val body = RequestBody.create(mediaType, soapXml)

                val request = Request.Builder()
                    .url("http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso") // Replace with actual API URL
                    .post(body)
                    .addHeader("Content-Type", "text/xml")
//                    .addHeader(
//                        "SOAPAction",
//                        "http://www.oorsprong.org/websamples.countryinfo/CountryCurrency"
//                    )
                    .build()



                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        onResponse("Error: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string() ?: "Error: Empty Response"
                        Log.d("SOAP_RESPONSE", responseBody)
                        onResponse(responseBody)
                    }
                })
            }
        }
    }
