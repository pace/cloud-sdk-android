package cloud.pace.sdk.appkit.persistence

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import cloud.pace.sdk.appkit.model.Car
import cloud.pace.sdk.appkit.utils.JWTUtils
import cloud.pace.sdk.idkit.IDKit
import net.openid.appauth.AuthState
import org.json.JSONException
import timber.log.Timber

interface SharedPreferencesModel {

    fun getBoolean(key: String, defValue: Boolean? = null): Boolean?
    fun putBoolean(key: String, value: Boolean)
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
    fun setTotpSecret(host: String? = null, key: String = UserRelatedConstants.PAYMENT_AUTHORIZE.value, totpSecret: TotpSecret)
    fun getTotpSecret(host: String? = null, key: String = UserRelatedConstants.PAYMENT_AUTHORIZE.value): TotpSecret?
    fun removeTotpSecret(host: String? = null, key: String = UserRelatedConstants.PAYMENT_AUTHORIZE.value)
    fun remove(key: String)
    fun migrateScopedUserValues()
}

class SharedPreferencesImpl(private val context: Context) : SharedPreferencesModel {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun getBoolean(key: String, defValue: Boolean?): Boolean? {
        val preferences = getPreferences(key) ?: return defValue
        return preferences.getBoolean(key, defValue ?: false)
    }

    override fun putBoolean(key: String, value: Boolean) {
        getPreferences(key)?.edit { putBoolean(key, value) }
    }

    override fun putString(key: String, value: String) {
        getPreferences(key)?.edit { putString(key, value) }
    }

    override fun getString(key: String, defValue: String?): String? {
        val preferences = getPreferences(key) ?: return defValue
        return preferences.getString(key, defValue)
    }

    override fun putStringSet(key: String, values: HashSet<String>) {
        getPreferences(key)?.edit { putStringSet(key, values) }
    }

    override fun getStringSet(key: String, defValues: HashSet<String>?): HashSet<String>? {
        return getPreferences(key)?.getStringSet(key, defValues) as? HashSet<String>
    }

    override fun putInt(key: String, value: Int) {
        getPreferences(key)?.edit { putInt(key, value) }
    }

    override fun getInt(key: String, defValue: Int?): Int? {
        val preferences = getPreferences(key)
        return if (preferences?.contains(key) == true) {
            preferences.getInt(key, defValue ?: 0)
        } else {
            null
        }
    }

    override fun putLong(key: String, value: Long) {
        getPreferences(key)?.edit { putLong(key, value) }
    }

    override fun getLong(key: String, defValue: Long?): Long? {
        val preferences = getPreferences(key)
        return if (preferences?.contains(key) == true) {
            preferences.getLong(key, defValue ?: 0L)
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
        putString(getTotpSecretPreferenceKey(UserRelatedConstants.SECRET.value, host, key), totpSecret.encryptedSecret)
        putInt(getTotpSecretPreferenceKey(UserRelatedConstants.DIGITS.value, host, key), totpSecret.digits)
        putInt(getTotpSecretPreferenceKey(UserRelatedConstants.PERIOD.value, host, key), totpSecret.period)
        putString(getTotpSecretPreferenceKey(UserRelatedConstants.ALGORITHM.value, host, key), totpSecret.algorithm)
    }

    override fun getTotpSecret(host: String?, key: String): TotpSecret? {
        val encryptedSecret = getString(getTotpSecretPreferenceKey(UserRelatedConstants.SECRET.value, host, key))
        val digits = getInt(getTotpSecretPreferenceKey(UserRelatedConstants.DIGITS.value, host, key))
        val period = getInt(getTotpSecretPreferenceKey(UserRelatedConstants.PERIOD.value, host, key))
        val algorithm = getString(getTotpSecretPreferenceKey(UserRelatedConstants.ALGORITHM.value, host, key))

        return if (encryptedSecret != null && digits != null && period != null && algorithm != null) {
            TotpSecret(encryptedSecret, digits, period, algorithm)
        } else null
    }

    override fun removeTotpSecret(host: String?, key: String) {
        val preferences = getPreferences(key)
        val totpSecretPrefs = preferences?.all?.filter { it.key.endsWith("_${UserRelatedConstants.PAYMENT_AUTHORIZE.value}") }
        preferences?.edit {
            totpSecretPrefs?.forEach {
                remove(it.key)
            }
        }
    }

    override fun remove(key: String) {
        getPreferences(key)?.edit { remove(key) }
    }

    override fun migrateScopedUserValues() {
        val isAlreadyMigrated = sharedPreferences.getBoolean(MIGRATED_USER_SCOPED_VALUES, false)
        if (isAlreadyMigrated) return

        // Get token from shared preferences
        val jsonString = sharedPreferences.getString(SESSION_CACHE, null) ?: return
        val authState = try {
            AuthState.jsonDeserialize(jsonString)
        } catch (e: JSONException) {
            Timber.e(e, "Failed retrieving session from SharedPreferences")
            return
        }
        val userPreferences = getUserPreferences(context, authState.accessToken)

        sharedPreferences.all.filter { isUserPreferenceValue(it.key) }.forEach {
            var isMigrated = false
            userPreferences?.edit {
                when (val value = it.value) {
                    is Int -> {
                        putInt(it.key, value)
                        isMigrated = true
                    }
                    is String -> {
                        putString(it.key, value)
                        isMigrated = true
                    }
                    is Boolean -> {
                        putBoolean(it.key, value)
                        isMigrated = true
                    }
                    is Long -> {
                        putLong(it.key, value)
                        isMigrated = true
                    }
                    is Float -> {
                        putFloat(it.key, value)
                        isMigrated = true
                    }
                    is Set<*> -> {
                        val set = value.asSetOfType<String>()
                        putStringSet(it.key, set)
                        isMigrated = true
                    }
                    else -> Timber.e("Error in scope user values migration: totp secret with invalid type (key: ${it.key} || value:${it.value}")
                }
            }
            if (isMigrated) {
                sharedPreferences.edit { remove(it.key) }
            }
        }

        sharedPreferences.edit { putBoolean(MIGRATED_USER_SCOPED_VALUES, true) }
    }

    private inline fun <reified T> Set<*>.asSetOfType(): Set<String>? =
        if (all { it is String })
            @Suppress("UNCHECKED_CAST")
            this as Set<String> else
            null

    private fun isUserPreferenceValue(key: String) = UserRelatedConstants.values().any { key.contains(it.value) }

    private fun getPreferences(key: String): SharedPreferences? {
        return when {
            !isUserPreferenceValue(key) -> sharedPreferences
            IDKit.isAuthorizationValid() -> getUserPreferences(context)
            else -> null
        }
    }

    companion object {
        private const val VIN = "vin"
        private const val FUEL_TYPE = "fuelType"
        private const val EXPECTED_AMOUNT = "expectedAmount"
        private const val MILEAGE = "mileage"
        private const val MIGRATED_USER_SCOPED_VALUES = "migratedUserScopedValues"
        const val SESSION_CACHE = "sessionCache"

        fun getTotpSecretPreferenceKey(which: String, host: String?, key: String) = which + (if (host != null) "_$host" else "") + "_$key"

        fun getSecureDataPreferenceKey(host: String, key: String) = "${UserRelatedConstants.SECURE_DATA.value}_${host}_$key"

        fun getDisableTimePreferenceKey(host: String) = "${UserRelatedConstants.DISABLE_TIME.value}_$host"

        /**
         * SharedPreferences file with user id as name that is used to save user id related preferences
         * User id is extracted from token
         */
        fun getUserPreferences(context: Context, token: String? = null): SharedPreferences? {
            val cachedToken = token ?: IDKit.cachedToken() ?: return null
            val userID = JWTUtils.getUserIDFromToken(cachedToken) ?: return null
            return context.getSharedPreferences(userID, Context.MODE_PRIVATE)
        }
    }
}

enum class UserRelatedConstants(val value: String) {
    SECRET("totp_secret_secret"),
    PERIOD("totp_secret_period"),
    DIGITS("totp_secret_digits"),
    ALGORITHM("totp_secret_algorithm"),
    SECURE_DATA("secure_data"),
    DISABLE_TIME("disable_time"),
    PAYMENT_AUTHORIZE("payment-authorize")
}

data class TotpSecret(val encryptedSecret: String, val digits: Int, val period: Int, val algorithm: String)
