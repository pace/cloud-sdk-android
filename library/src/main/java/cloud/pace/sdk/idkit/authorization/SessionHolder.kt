package cloud.pace.sdk.idkit.authorization

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl
import cloud.pace.sdk.idkit.model.OIDConfiguration
import cloud.pace.sdk.idkit.model.toAuthorizationServiceConfiguration
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import net.openid.appauth.AuthState
import org.json.JSONException
import timber.log.Timber

class SessionHolder(private val context: Context) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val lock = ReentrantReadWriteLock()
    var session = loadSession()
        private set

    fun isAuthorizationValid() = lock.read { session?.isAuthorized ?: false }

    fun cachedToken() = lock.read { session?.accessToken }

    fun <T> withSession(block: (AuthState?) -> T): T = lock.write { block(session) }

    fun updateSession(configuration: OIDConfiguration) = lock.write {
        session = loadSession() ?: AuthState(configuration.toAuthorizationServiceConfiguration())
    }

    fun persistSession() = lock.read {
        Timber.i("Persisting session to SharedPreferences")
        sharedPreferences.edit { putString(SESSION_CACHE, session?.jsonSerializeString()) }
    }

    fun clearSessionAndPreferences() = lock.write {
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
