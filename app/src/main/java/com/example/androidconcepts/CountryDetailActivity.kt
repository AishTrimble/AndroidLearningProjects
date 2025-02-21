package com.example.androidconcepts

import android.os.Bundle
import android.util.Log
import android.widget.TextView
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

class CountryDetailActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.countrydetail)
        val countryName = intent.getStringExtra("COUNTRY_NAME")
        val languageName = intent.getStringExtra("LANGUAGE_NAME")

        val isoCode = intent.getStringExtra("ISO_CODE")
        val languageisoCode = intent.getStringExtra("LANGUAGE_ISO_CODE")


        val textView: TextView = findViewById(R.id.textView2)






        if(countryName != null && isoCode != null){
            sendSoapRequest(isoCode) { response ->
                val parsedResponse = parseXmlWithXmlPullParser(response)

                runOnUiThread {
                    textView.text = parsedResponse
                }
            }
        }

        else if(languageName != null){
            textView.text = "Language: $languageName\nISO Code: $languageisoCode"
        }
}
    private fun sendSoapRequest(isoCode: String,onResponse: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()


            val soapXml = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <CountryCurrency xmlns="http://www.oorsprong.org/websamples.countryinfo">
                      <sCountryISOCode>$isoCode</sCountryISOCode>
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


}
