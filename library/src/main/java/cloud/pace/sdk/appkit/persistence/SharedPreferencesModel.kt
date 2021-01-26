package cloud.pace.sdk.appkit.persistence

import android.content.SharedPreferences
import cloud.pace.sdk.appkit.model.Car

interface SharedPreferencesModel {

    fun putString(key: String, value: String)
    fun getString(key: String, defValue: String? = null): String?
    fun putStringSet(key: String, values: HashSet<String>)
    fun getStringSet(key: String, defValues: HashSet<String>? = null): HashSet<String>?
    fun putInt(key: String, value: Int)
    fun getInt(key: String, defValue: Int? = null): Int?
    fun putLong(key: String, value: Long)
    fun getLong(key: String, defValue: Long? = null): Long?
    fun setCar(car: Car)
    fun getCar(): Car
    fun remove(key: String)
}

class SharedPreferencesImpl(private val sharedPreferences: SharedPreferences) : SharedPreferencesModel {

    override fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun getString(key: String, defValue: String?): String? {
        return sharedPreferences.getString(key, defValue)
    }

    override fun putStringSet(key: String, values: HashSet<String>) {
        sharedPreferences.edit().putStringSet(key, values).apply()
    }

    override fun getStringSet(key: String, defValues: HashSet<String>?): HashSet<String>? {
        return sharedPreferences.getStringSet(key, defValues) as? HashSet<String>
    }

    override fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    override fun getInt(key: String, defValue: Int?): Int? {
        return if (sharedPreferences.contains(key)) {
            sharedPreferences.getInt(key, defValue ?: 0)
        } else {
            null
        }
    }

    override fun putLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    override fun getLong(key: String, defValue: Long?): Long? {
        return if (sharedPreferences.contains(key)) {
            sharedPreferences.getLong(key, defValue ?: 0L)
        } else {
            null
        }
    }

    override fun setCar(car: Car) {
        car.vin?.let { putString(VIN, it) }
        car.fuelTyp?.let { putString(FUEL_TYPE, it) }
        car.expectedAmount?.let { putInt(EXPECTED_AMOUNT, it) }
        car.mileage?.let { putInt(MILEAGE, it) }
    }

    override fun getCar(): Car {
        return Car(getString(VIN), getString(FUEL_TYPE), getInt(EXPECTED_AMOUNT), getInt(MILEAGE))
    }

    override fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    companion object {
        private const val VIN = "vin"
        private const val FUEL_TYPE = "fuelType"
        private const val EXPECTED_AMOUNT = "expectedAmount"
        private const val MILEAGE = "mileage"

        const val SECRET = "totp_secret_secret"
        const val PERIOD = "totp_secret_period"
        const val DIGITS = "totp_secret_digits"
        const val ALGORITHM = "totp_secret_algorithm"
        const val SECURE_DATA = "secure_data"
        const val DISABLE_TIME = "disable_time"
    }
}
