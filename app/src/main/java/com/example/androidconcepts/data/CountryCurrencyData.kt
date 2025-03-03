package com.example.androidconcepts.data


import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.StringReader

class CountryCurrencyData {

    companion object {
        private val client = OkHttpClient()

        suspend fun fetchCountryCurrency(countryCode: String): String {
            return withContext(Dispatchers.IO) {
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
                    .url("http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso")
                    .post(body)
                    .addHeader("Content-Type", "text/xml")
                    .build()

                try {
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: "Error: Empty Response"
                    Log.d("SOAP_RESPONSE", responseBody)
                    return@withContext parseXmlWithXmlPullParser(responseBody)
                } catch (e: IOException) {
                    return@withContext "Error: ${e.message}"
                }
            }
        }

        // XML Parser to extract the flag URL
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
                        }
                    }

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
    }
}
