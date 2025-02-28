import com.example.androidconcepts.CountryInfo

class CountryRepository(private val dbHelper: DBHelper) {

    fun saveCountryInfo(country: CountryInfo) {
        dbHelper.insertCountryInfo(country)
    }

    fun getAllCountries(): List<CountryInfo> {
        return dbHelper.getAllCountries()
    }
}
