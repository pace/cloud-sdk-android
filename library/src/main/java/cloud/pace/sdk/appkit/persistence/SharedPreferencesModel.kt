package cloud.pace.sdk.appkit.persistence

import android.content.SharedPreferences
import androidx.core.content.edit
import cloud.pace.sdk.appkit.model.Car
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl.Companion.PAYMENT_AUTHORIZE

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
    fun setTotpSecret(host: String? = null, key: String = PAYMENT_AUTHORIZE, totpSecret: TotpSecret)
    fun getTotpSecret(host: String? = null, key: String = PAYMENT_AUTHORIZE): TotpSecret?
    fun removeTotpSecret(host: String? = null, key: String = PAYMENT_AUTHORIZE)
    fun remove(key: String)
}

class SharedPreferencesImpl(private val sharedPreferences: SharedPreferences) : SharedPreferencesModel {

    override fun putString(key: String, value: String) {
        sharedPreferences.edit { putString(key, value) }
    }

    override fun getString(key: String, defValue: String?): String? {
        return sharedPreferences.getString(key, defValue)
    }

    override fun putStringSet(key: String, values: HashSet<String>) {
        sharedPreferences.edit { putStringSet(key, values) }
    }

    override fun getStringSet(key: String, defValues: HashSet<String>?): HashSet<String>? {
        return sharedPreferences.getStringSet(key, defValues) as? HashSet<String>
    }

    override fun putInt(key: String, value: Int) {
        sharedPreferences.edit { putInt(key, value) }
    }

    override fun getInt(key: String, defValue: Int?): Int? {
        return if (sharedPreferences.contains(key)) {
            sharedPreferences.getInt(key, defValue ?: 0)
        } else {
            null
        }
    }

    override fun putLong(key: String, value: Long) {
        sharedPreferences.edit { putLong(key, value) }
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

    override fun setTotpSecret(host: String?, key: String, totpSecret: TotpSecret) {
        putString(getTotpSecretPreferenceKey(SECRET, host, key), totpSecret.encryptedSecret)
        putInt(getTotpSecretPreferenceKey(DIGITS, host, key), totpSecret.digits)
        putInt(getTotpSecretPreferenceKey(PERIOD, host, key), totpSecret.period)
        putString(getTotpSecretPreferenceKey(ALGORITHM, host, key), totpSecret.algorithm)
    }

    override fun getTotpSecret(host: String?, key: String): TotpSecret? {
        val encryptedSecret = getString(getTotpSecretPreferenceKey(SECRET, host, key))
        val digits = getInt(getTotpSecretPreferenceKey(DIGITS, host, key))
        val period = getInt(getTotpSecretPreferenceKey(PERIOD, host, key))
        val algorithm = getString(getTotpSecretPreferenceKey(ALGORITHM, host, key))

        return if (encryptedSecret != null && digits != null && period != null && algorithm != null) {
            TotpSecret(encryptedSecret, digits, period, algorithm)
        } else null
    }

    override fun removeTotpSecret(host: String?, key: String) {
        val totpSecretPrefs = sharedPreferences.all.filter { it.key.endsWith("_$PAYMENT_AUTHORIZE") }
        sharedPreferences.edit {
            totpSecretPrefs.forEach {
                remove(it.key)
            }
        }
    }

    override fun remove(key: String) {
        sharedPreferences.edit { remove(key) }
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
        const val PAYMENT_AUTHORIZE = "payment-authorize"
        const val SESSION_CACHE = "sessionCache"

        fun getTotpSecretPreferenceKey(which: String, host: String?, key: String) = which + (if (host != null) "_$host" else "") + "_$key"

        fun getSecureDataPreferenceKey(host: String, key: String) = "${SECURE_DATA}_${host}_$key"

        fun getDisableTimePreferenceKey(host: String) = "${DISABLE_TIME}_$host"
    }
}

data class TotpSecret(val encryptedSecret: String, val digits: Int, val period: Int, val algorithm: String)
