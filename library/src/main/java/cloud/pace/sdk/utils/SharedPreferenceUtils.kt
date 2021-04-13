package cloud.pace.sdk.utils

import android.content.Context
import androidx.preference.PreferenceManager
import net.openid.appauth.AuthState
import org.json.JSONException
import timber.log.Timber

object SharedPreferenceUtils {

    private const val SESSION_CACHE = "sessionCache"

    fun persistSession(context: Context, authState: AuthState) {
        Timber.i("Persisting session to SharedPreferences")
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(SESSION_CACHE, authState.jsonSerializeString())
            .apply()
    }

    fun loadSession(context: Context): AuthState? {
        Timber.i("Loading session from SharedPreferences")
        val jsonString = PreferenceManager.getDefaultSharedPreferences(context).getString(SESSION_CACHE, null)
        return if (!jsonString.isNullOrEmpty()) {
            try {
                AuthState.jsonDeserialize(jsonString)
            } catch (jsonException: JSONException) {
                Timber.e(jsonException, "Failed retrieving session from SharedPreferences")
                null
            }
        } else {
            null
        }
    }
}
