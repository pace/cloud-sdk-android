package cloud.pace.sdk.idkit.authorization

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl
import cloud.pace.sdk.idkit.model.OIDConfiguration
import cloud.pace.sdk.idkit.model.toAuthorizationServiceConfiguration
import net.openid.appauth.AuthState
import org.json.JSONException
import timber.log.Timber

class SessionHolder(private val context: Context) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    var session = loadSession()
        private set

    fun isAuthorizationValid() = session?.isAuthorized ?: false

    fun cachedToken() = session?.accessToken

    fun updateSession(configuration: OIDConfiguration) {
        session = loadSession() ?: AuthState(configuration.toAuthorizationServiceConfiguration())
    }

    fun persistSession() {
        Timber.i("Persisting session to SharedPreferences")
        sharedPreferences.edit { putString(SESSION_CACHE, session?.jsonSerializeString()) }
    }

    fun clearSessionAndPreferences() {
        SharedPreferencesImpl.removeUserPreferences(context, cachedToken())

        val serviceConfiguration = session?.authorizationServiceConfiguration
        if (serviceConfiguration != null) {
            val clearedState = AuthState(serviceConfiguration)
            val lastRegistrationResponse = session?.lastRegistrationResponse
            if (lastRegistrationResponse != null) {
                clearedState.update(lastRegistrationResponse)
            }
            session = clearedState
        }

        persistSession()
    }

    private fun loadSession(): AuthState? {
        Timber.i("Loading session from SharedPreferences")
        val jsonString = sharedPreferences.getString(SESSION_CACHE, null)
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

    companion object {
        private const val SESSION_CACHE = "sessionCache"
    }
}
