package com.example.androidconcepts.activity

import CountryRepository
import DBHelper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.androidconcepts.R
import com.example.androidconcepts.model.CountryInfo

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
import java.io.InputStream
import java.io.StringReader
import java.net.URL
import java.util.concurrent.Executors

class CountryDetailActivity : AppCompatActivity() {
    private var response1: String? = null
    private var response2: String? = null
    private var response3: String? = null
    private lateinit var flagImageView: ImageView

    private lateinit var textView: TextView
    private lateinit var dbHelper: DBHelper
    private lateinit var countryRepository: CountryRepository
    private var isoCode: String? = null
    private var countryName: String? = null
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            when (result.data?.getStringExtra("SOURCE_ACTIVITY")) {
                "CountryCapitalActivity" -> {
                    response2 = result.data?.getStringExtra("CAPITAL_RESPONSE")
                    Log.d("Activity2_Response", "Received from Activity 3: $response2")
                    // Only start Activity 4 after Activity 3 completes
                    updateTextView()
                    //checkAndSaveData()
                    startActivity4()


                }

                "CountryFlagImgActivity" -> {
                    response3 = result.data?.getStringExtra("FLAG_RESPONSE")
                    Log.d("Activity2_Response", "Received from Activity 4: $response3")
                    updateTextView()
                    //updateImageView()

                    checkAndSaveData()
                }
            }

        } else {
            Log.e("Activity2_Error", "No valid response from launched activity")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.countrydetail)
        textView = findViewById(R.id.textView2)
        //flagImageView = findViewById(R.id.imageView)

        dbHelper = DBHelper(this)
        countryRepository = CountryRepository(dbHelper)
        countryName = intent.getStringExtra("COUNTRY_NAME")
        val languageName = intent.getStringExtra("LANGUAGE_NAME")

        isoCode = intent.getStringExtra("ISO_CODE")
        val languageisoCode = intent.getStringExtra("LANGUAGE_ISO_CODE")

        val intent = Intent(this, CountryCapitalActivity::class.java)
        intent.putExtra("ISO_CODE", isoCode)
        activityResultLauncher.launch(intent)

        if (countryName != null && isoCode != null) {

            // Step 1: Start SOAP Request
            sendSoapRequest(isoCode!!) { response ->
                response1 = parseXmlWithXmlPullParser(response)
                Log.d("SOAP_RESPONSE", "Response1: $response1")

                // Step 2: Start Activity 3
                startActivity3()

            }
//            val intent3 = Intent(this, CountryCapitalActivity::class.java)
//            intent3.putExtra("ISO_CODE", isoCode)
//            intent3.putExtra("SOURCE_ACTIVITY", "CountryCapitalActivity") // Add source identifier
//
//
//            // Start Activity 4 (Another Response)
//            val intent4 = Intent(this, CountryFlagImgActivity::class.java)
//            intent4.putExtra("ISO_CODE", isoCode)
//            intent4.putExtra("SOURCE_ACTIVITY", "CountryFlagImgActivity") // Add source identifier
//            sendSoapRequest(isoCode) { response ->
//                val parsedResponse = parseXmlWithXmlPullParser(response)
//                 response1=parsedResponse
//
//                checkAndSaveData()
//                updateTextView()
////                runOnUiThread {
////                    textView.text = parsedResponse+"\n"+cap
////                }
//            }
//            activityResultLauncher.launch(intent3)
//            activityResultLauncher.launch(intent4)

        } else if (languageName != null) {
            textView.text = "Language: $languageName\nISO Code: $languageisoCode"
        }


    }

    private fun startActivity3() {
        val intent3 = Intent(this, CountryCapitalActivity::class.java)
        intent3.putExtra("ISO_CODE", isoCode)
        intent3.putExtra("SOURCE_ACTIVITY", "Activity3") // Add source identifier
        activityResultLauncher.launch(intent3)
    }

    private fun startActivity4() {
        val intent4 = Intent(this, CountryFlagImgActivity::class.java)
        intent4.putExtra("ISO_CODE", isoCode)
        intent4.putExtra("SOURCE_ACTIVITY", "Activity4") // Add source identifier
        activityResultLauncher.launch(intent4)
    }

    private fun checkAndSaveData() {
        if (response1 != null && response2 != null && response3 != null) {
            val countryInfo = CountryInfo(
                countryISOCode = intent.getStringExtra("ISO_CODE") ?: "UNKNOWN",
                countryName = intent.getStringExtra("COUNTRY_NAME")!!,
                countryCurrency = response1,
                countryCapital = response2, // Now response2 will not be null
                countryFlagImageUrl = response3
            )

            // Save to database
            countryRepository.saveCountryInfo(countryInfo)
            //updateTextView()


            // Verify the data is saved
            val countryList = countryRepository.getAllCountries()
            for (country in countryList) {
                Log.d(
                    "Database",
                    "Country: ${country.countryName}, Capital: ${country.countryCapital}, FlagUrl : ${country.countryFlagImageUrl}"
                )
            }
        }
    }


    private fun updateTextView() {
        runOnUiThread {
            val resultText =
                "Currency Details:\n$response1\n\nCountry Capital:\n$response2\n\nCountryFlagImgUrl : \n$response3"
            textView.text = resultText
        }
    }

    private fun updateImageView() {
        runOnUiThread {
            response3?.let { url ->
                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    val bitmap = downloadImage(url)
                    runOnUiThread {
                        Log.d("image", "flag download")
                        flagImageView.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }


    private fun downloadImage(url: String): Bitmap? {
        return try {
            val inputStream: InputStream = URL(url).openStream()
            BitmapFactory.decodeStream(inputStream)
            //Log.d("image")
        } catch (e: Exception) {
            Log.e("Error", e.message ?: "Error downloading image")
            e.printStackTrace()
            null
        }
    }

    //    private fun updateTextView() {
//        TODO("Not yet implemented")
//        runOnUiThread {
//            val resultText = "Response from Activity 2:\n$response1\n\nResponse from Activity 3:\n$response2"
//            textView.text = resultText
//        }
//    }
    private fun sendSoapRequest(isoCode: String, onResponse: (String) -> Unit) {
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
                    }
                }
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
