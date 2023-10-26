package car.pace.cofu.data

import android.content.SharedPreferences
import androidx.core.content.edit
import cloud.pace.sdk.utils.asSetOfType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class SharedPreferencesRepository @Inject constructor(
    val sharedPreferences: SharedPreferences
) {

    inline fun <reified T> getValue(key: String, defaultValue: T) = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, preferenceKey ->
            if (key == preferenceKey) {
                val value = sharedPreferences.all[key] ?: defaultValue
                if (value is T) {
                    trySend(value)
                }
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        // Initial value
        if (sharedPreferences.contains(key)) {
            val value = sharedPreferences.all[key] ?: defaultValue
            if (value is T) {
                send(value)
            }
        }

        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.buffer(Channel.UNLIMITED) // So trySend never fails

    fun getBoolean(key: String, defaultValue: Boolean) = sharedPreferences.getBoolean(key, defaultValue)

    fun getFloat(key: String, defaultValue: Float) = sharedPreferences.getFloat(key, defaultValue)

    fun getInt(key: String, defaultValue: Int) = sharedPreferences.getInt(key, defaultValue)

    fun getLong(key: String, defaultValue: Long) = sharedPreferences.getLong(key, defaultValue)

    fun getString(key: String, defaultValue: String) = sharedPreferences.getString(key, defaultValue)

    fun getStringSet(key: String, defaultValue: Set<String>) = sharedPreferences.getStringSet(key, defaultValue)

    fun <T> putValue(key: String, value: T) {
        sharedPreferences.edit {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is String -> putString(key, value)
                is Set<*> -> putStringSet(key, value.asSetOfType<String>())
            }
        }
    }

    companion object {
        const val PREF_KEY_ONBOARDING_DONE = "onboardingDone"
        const val PREF_KEY_FUEL_TYPE = "fuelType"
    }
}
