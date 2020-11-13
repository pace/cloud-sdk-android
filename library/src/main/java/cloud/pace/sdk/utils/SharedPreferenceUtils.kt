package cloud.pace.sdk.utils

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import cloud.pace.sdk.utils.Constants.TAG
import net.openid.appauth.AuthState
import org.json.JSONException

object SharedPreferenceUtils {

    private const val SESSION_CACHE = "sessionCache"

    fun persistSession(context: Context, authState: AuthState) {
        Log.i(TAG, "Persisting session to SharedPreferences")
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(SESSION_CACHE, authState.jsonSerializeString())
            .apply()
    }

    fun loadSession(context: Context): AuthState? {
        Log.i(TAG, "Loading session from SharedPreferences")
        val jsonString = PreferenceManager.getDefaultSharedPreferences(context).getString(SESSION_CACHE, null)
        return if (!jsonString.isNullOrEmpty()) {
            try {
                AuthState.jsonDeserialize(jsonString)
            } catch (jsonException: JSONException) {
                Log.e(TAG, "Failed retrieving session from SharedPreferences", jsonException)
                null
            }
        } else {
            null
        }
    }
}
