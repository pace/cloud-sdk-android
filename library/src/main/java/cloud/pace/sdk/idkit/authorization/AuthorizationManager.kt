package cloud.pace.sdk.idkit.authorization

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import cloud.pace.sdk.api.API
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl.Companion.SESSION_CACHE
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.*
import cloud.pace.sdk.idkit.userinfo.UserInfoApiClient
import cloud.pace.sdk.idkit.userinfo.UserInfoResponse
import cloud.pace.sdk.utils.*
import net.openid.appauth.*
import org.json.JSONException
import timber.log.Timber

internal class AuthorizationManager(
    private val context: Context,
    private val authorizationService: AuthorizationService,
    private val sharedPreferencesModel: SharedPreferencesModel
) : CloudSDKKoinComponent, LifecycleObserver {

    private lateinit var configuration: OIDConfiguration
    private lateinit var authorizationRequest: AuthorizationRequest
    private lateinit var session: AuthState

    private var additionalCaching = true

    internal fun setup(configuration: OIDConfiguration, additionalCaching: Boolean = true) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        this.configuration = configuration
        this.additionalCaching = additionalCaching
        authorizationRequest = createAuthorizationRequest()

        if (additionalCaching) {
            loadSession()?.let {
                session = it
            }
        }
    }

    internal fun discoverConfiguration(issuerUri: String, completion: (Completion<ServiceConfiguration>) -> Unit) {
        AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(issuerUri)) { configuration, exception ->
            when {
                exception != null -> {
                    Timber.e(exception, "Failed to discover configuration")
                    completion(Failure(exception))
                }
                configuration != null -> {
                    Timber.i("Configuration discovery successful")
                    completion(Success(ServiceConfiguration(configuration.authorizationEndpoint, configuration.tokenEndpoint, configuration.endSessionEndpoint, configuration.registrationEndpoint)))
                }
                else -> {
                    val throwable = FailedRetrievingConfigurationWhileDiscovering
                    Timber.e(throwable, "Failed to discover configuration")
                    completion(Failure(throwable))
                }
            }
        }
    }

    internal suspend fun authorize(activity: AppCompatActivity, completion: (Completion<String?>) -> Unit) {
        when (val result = activity.getResultFor(authorize())) {
            is Ok -> result.data?.let { handleAuthorizationResponse(it, completion) } ?: completion(Failure(InternalError))
            is Canceled -> completion(Failure(OperationCanceled))
        }
    }

    internal suspend fun authorize(fragment: Fragment, completion: (Completion<String?>) -> Unit) {
        when (val result = fragment.getResultFor(authorize())) {
            is Ok -> result.data?.let { handleAuthorizationResponse(it, completion) } ?: completion(Failure(InternalError))
            is Canceled -> completion(Failure(OperationCanceled))
        }
    }

    internal fun authorize(completedActivity: Class<*>, canceledActivity: Class<*>) {
        authorizationService.performAuthorizationRequest(
            authorizationRequest,
            PendingIntent.getActivity(context, 0, Intent(context, completedActivity), 0),
            PendingIntent.getActivity(context, 0, Intent(context, canceledActivity), 0)
        )
    }

    internal fun authorize() = authorizationService.getAuthorizationRequestIntent(authorizationRequest)

    internal fun handleAuthorizationResponse(intent: Intent, completion: (Completion<String?>) -> Unit) {
        val response = AuthorizationResponse.fromIntent(intent)
        val exception = AuthorizationException.fromIntent(intent)

        when {
            exception != null -> {
                session.update(response, exception)
                Timber.e(exception, "Failed to handle authorization response")
                completion(Failure(exception))
            }
            response != null -> {
                session.update(response, exception)
                performTokenRequest(response.createTokenExchangeRequest(), completion)
            }
            else -> {
                val throwable = FailedRetrievingSessionWhileAuthorizing
                Timber.e(throwable, "Failed to handle authorization response")
                completion(Failure(throwable))
            }
        }
    }

    internal fun refreshToken(force: Boolean = false, completion: (Completion<String?>) -> Unit) {
        if (isAuthorizationValid()) {
            if (force) {
                session.needsTokenRefresh = true
            }
            performTokenRequest(session.createTokenRefreshRequest(), completion)
        } else {
            completion(Failure(InvalidSession))
        }
    }

    internal suspend fun endSession(activity: AppCompatActivity, completion: (Completion<Unit>) -> Unit) {
        endSession()?.let { intent ->
            when (val result = activity.getResultFor(intent)) {
                is Ok -> result.data?.let { IDKit.handleEndSessionResponse(it, completion) } ?: completion(Failure(InternalError))
                is Canceled -> completion(Failure(OperationCanceled))
            }
        } ?: completion(Failure(FailedRetrievingSessionWhileEnding))
    }

    internal suspend fun endSession(fragment: Fragment, completion: (Completion<Unit>) -> Unit) {
        endSession()?.let { intent ->
            when (val result = fragment.getResultFor(intent)) {
                is Ok -> result.data?.let { IDKit.handleEndSessionResponse(it, completion) } ?: completion(Failure(InternalError))
                is Canceled -> completion(Failure(OperationCanceled))
            }
        } ?: completion(Failure(FailedRetrievingSessionWhileEnding))
    }

    internal fun endSession(completedActivity: Class<*>, canceledActivity: Class<*>): Boolean {
        return createEndSessionRequest()?.let {
            authorizationService.performEndSessionRequest(
                it,
                PendingIntent.getActivity(context, 0, Intent(context, completedActivity), 0),
                PendingIntent.getActivity(context, 0, Intent(context, canceledActivity), 0)
            )
            true
        } ?: false
    }

    internal fun endSession() = createEndSessionRequest()?.let { authorizationService.getEndSessionRequestIntent(it) }

    internal fun handleEndSessionResponse(intent: Intent, completion: (Completion<Unit>) -> Unit) {
        val response = EndSessionResponse.fromIntent(intent)
        val exception = AuthorizationException.fromIntent(intent)

        when {
            exception != null -> {
                Timber.e(exception, "Failed to handle end session response")
                completion(Failure(exception))
            }
            response != null -> {
                IDKit.disableBiometricAuthentication()
                API.addAuthorizationHeader(null)

                val serviceConfiguration = session.authorizationServiceConfiguration
                if (serviceConfiguration != null) {
                    val clearedState = AuthState(serviceConfiguration)
                    val lastRegistrationResponse = session.lastRegistrationResponse
                    if (lastRegistrationResponse != null) {
                        clearedState.update(lastRegistrationResponse)
                    }
                    session = clearedState
                }
                persistSession(session)

                completion(Success(Unit))
            }
            else -> {
                val throwable = FailedRetrievingSessionWhileEnding
                Timber.e(throwable, "Failed to handle end session response")
                completion(Failure(throwable))
            }
        }
    }

    internal fun isAuthorizationValid() = session.isAuthorized

    internal fun containsAuthorizationResponse(intent: Intent) = intent.hasExtra(AuthorizationResponse.EXTRA_RESPONSE)

    internal fun containsEndSessionResponse(intent: Intent) = intent.hasExtra(EndSessionResponse.EXTRA_RESPONSE)

    internal fun containsException(intent: Intent) = intent.hasExtra(AuthorizationException.EXTRA_EXCEPTION)

    internal fun cachedToken() = if (::session.isInitialized) session.accessToken else null

    internal fun userInfo(accessToken: String, completion: (Completion<UserInfoResponse>) -> Unit) {
        configuration.userInfoEndpoint.let {
            if (it != null) {
                UserInfoApiClient(it, accessToken).getUserInfo(completion)
            } else {
                val throwable = UserEndpointNotDefined
                Timber.e(throwable)
                completion(Failure(throwable))
            }
        }
    }

    private fun createAuthorizationRequest(): AuthorizationRequest {
        val serviceConfiguration = getAuthorizationServiceConfiguration()
        session = AuthState(serviceConfiguration)

        return AuthorizationRequest.Builder(serviceConfiguration, configuration.clientId, configuration.responseType, Uri.parse(configuration.redirectUri))
            .setScopes(configuration.scopes?.plus("openid") ?: listOf("openid")) // Make sure that 'openid' is passed as scope so that the idToken for the end session request is returned
            .setAdditionalParameters(configuration.additionalParameters)
            .build()
    }

    private fun createEndSessionRequest(): EndSessionRequest? {
        return session.idToken?.let {
            EndSessionRequest.Builder(
                getAuthorizationServiceConfiguration(),
                it,
                Uri.parse(configuration.redirectUri)
            ).build()
        }
    }

    private fun getAuthorizationServiceConfiguration(): AuthorizationServiceConfiguration {
        return AuthorizationServiceConfiguration(
            Uri.parse(configuration.authorizationEndpoint),
            Uri.parse(configuration.tokenEndpoint),
            null,
            Uri.parse(configuration.endSessionEndpoint)
        )
    }

    private fun performTokenRequest(request: TokenRequest, completion: (Completion<String?>) -> Unit) {
        Timber.i("Trying to refresh token...")

        val clientSecret = configuration.clientSecret
        val clientAuthentication: ClientAuthentication = if (clientSecret != null) {
            ClientSecretBasic(clientSecret)
        } else {
            try {
                session.clientAuthentication
            } catch (e: ClientAuthentication.UnsupportedAuthenticationMethod) {
                Timber.e(e, "Token request cannot be made, client authentication for the token endpoint could not be constructed")
                completion(Failure(e))
                return
            }
        }

        authorizationService.performTokenRequest(request, clientAuthentication) { tokenResponse, exception ->
            handleTokenResponse(tokenResponse, exception, completion)
        }
    }

    private fun handleTokenResponse(tokenResponse: TokenResponse?, exception: AuthorizationException?, completion: (Completion<String?>) -> Unit) {
        session.update(tokenResponse, exception)
        if (additionalCaching) {
            persistSession(session)
        }

        when {
            exception != null -> {
                Timber.e(exception, "Failed to handle token response")
                completion(Failure(exception))
            }
            tokenResponse != null -> {
                session.accessToken?.let { API.addAuthorizationHeader(it) }
                completion(Success(session.accessToken))
                Timber.i("Token refresh successful")
            }
            else -> {
                val throwable = FailedRetrievingSessionWhileAuthorizing
                Timber.e(throwable, "Failed to handle token response")
                completion(Failure(throwable))
            }
        }
    }

    private fun persistSession(authState: AuthState) {
        Timber.i("Persisting session to SharedPreferences")
        sharedPreferencesModel.putString(SESSION_CACHE, authState.jsonSerializeString())
    }

    private fun loadSession(): AuthState? {
        Timber.i("Loading session from SharedPreferences")
        val jsonString = sharedPreferencesModel.getString(SESSION_CACHE)
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

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun dispose() {
        // This must be called to avoid memory leaks.
        authorizationService.dispose()
    }
}
