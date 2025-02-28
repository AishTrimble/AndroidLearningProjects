import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.androidconcepts.CountryInfo

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "CountryDatabase.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "CountryInfo"
        private const val COLUMN_ID = "id"
        private const val COLUMN_ISO_CODE = "isoCode"
        private const val COLUMN_COUNTRY_NAME = "countryName"
        private const val COLUMN_CAPITAL_RESPONSE = "capitalResponse"
        private const val COLUMN_CURRENCY = "currency"
        private const val COLUMN_FLAG_URL = "flagUrl"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ISO_CODE TEXT NOT NULL,
                $COLUMN_COUNTRY_NAME TEXT NOT NULL,
                $COLUMN_CAPITAL_RESPONSE TEXT,
                $COLUMN_CURRENCY TEXT,
                $COLUMN_FLAG_URL TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Insert country data
    fun insertCountryInfo(country: CountryInfo): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ISO_CODE, country.countryISOCode)
            put(COLUMN_COUNTRY_NAME, country.countryName)
            put(COLUMN_CAPITAL_RESPONSE, country.countryCapital)
            put(COLUMN_CURRENCY, country.countryCurrency)
            put(COLUMN_FLAG_URL, country.countryFlagImageUrl)
        }
        Log.d("Insert", "success")
        return db.insert(TABLE_NAME, null, values)
    }

    // Fetch all countries
    fun getAllCountries(): List<CountryInfo> {
        val countryList = mutableListOf<CountryInfo>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        while (cursor.moveToNext()) {
            val country = CountryInfo(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                countryISOCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ISO_CODE)),
                countryName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COUNTRY_NAME)),
                countryCapital = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAPITAL_RESPONSE)),
                countryCurrency = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CURRENCY)),
                countryFlagImageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FLAG_URL))
            )
            countryList.add(country)
        }
        cursor.close()
        return countryList
    }
}
