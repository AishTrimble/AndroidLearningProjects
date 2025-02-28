package com.example.androidconcepts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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

class CountryFlagImgActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.country_capital_activity)
        val isoCode = intent.getStringExtra("ISO_CODE")
        Log.d("string","capital")
        if (isoCode != null) {
            Log.d("isocodecapital",isoCode)

            sendSoapRequest(isoCode) { response ->
                val parsedResponse = parseXmlWithXmlPullParser(response)
                Log.d("parsedResponse",parsedResponse)

                val resultIntent = Intent()
                resultIntent.putExtra("FLAG_RESPONSE", parsedResponse)
                resultIntent.putExtra("SOURCE_ACTIVITY", "CountryFlagImgActivity")

                setResult(RESULT_OK, resultIntent)
                finish()
                // Send the parsed response to another activity
//                val intent = Intent(this, CountryDetailActivity::class.java)
//                intent.putExtra("CAPITAL_RESPONSE", parsedResponse)
//                startActivity(intent)
            }
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
            //var isoCode: String? = null
            var ImageUrl: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
//                        "sISOCode", "m:sISOCode" -> {
//                            if (parser.next() == XmlPullParser.TEXT) isoCode = parser.text
//                        }

                        "m:CountryFlagResult","CountryFlagResult" -> {
                            if (parser.next() == XmlPullParser.TEXT) ImageUrl = parser.text
                            Log.d("Cap","Capital")

                        }
                    }
                }
//
                eventType = parser.next()
            }
            return ImageUrl ?: "Error: Could not extract capital city"
        } catch (e: Exception) {

            return "Error parsing response: ${e.localizedMessage}"
        }
    }


    private fun sendSoapRequest(countryCode: String, onResponse: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()

            val soapXml = """
         <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <CountryFlag xmlns="http://www.oorsprong.org/websamples.countryinfo">
                  <sCountryISOCode>$countryCode</sCountryISOCode>
                </CountryFlag>
              </soap:Body>
            </soap:Envelope>
            """.trimIndent()


            val mediaType = "text/xml; charset=utf-8".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, soapXml)

            val request = Request.Builder()
                .url("http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso") // Replace with actual API URL
                .post(body)
                .addHeader("Content-Type", "text/xml")
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