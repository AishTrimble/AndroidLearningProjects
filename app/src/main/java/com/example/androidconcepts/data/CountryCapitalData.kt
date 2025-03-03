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

class CountryCapitalData {
    companion object {

        private val client = OkHttpClient()

        // Function to fetch capital city
        suspend fun fetchCountryCapital(countryCode: String): String {
            return withContext(Dispatchers.IO) {
                val soapXml = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <CapitalCity xmlns="http://www.oorsprong.org/websamples.countryinfo">
                      <sCountryISOCode>$countryCode</sCountryISOCode>
                    </CapitalCity>
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

        // XML Parser to extract the capital city
        private fun parseXmlWithXmlPullParser(xmlResponse: String): String {
            return try {
                val factory = XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
                parser.setInput(StringReader(xmlResponse))

                var eventType = parser.eventType
                var capitalName: String? = null

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        when (parser.name) {
                            "m:CapitalCityResult", "CapitalCityResult" -> {
                                if (parser.next() == XmlPullParser.TEXT) capitalName = parser.text
                            }
                        }
                    }
                    eventType = parser.next()
                }
                capitalName ?: "Error: Could not extract capital city"
            } catch (e: Exception) {
                "Error parsing response: ${e.localizedMessage}"
            }
        }
    }
}
