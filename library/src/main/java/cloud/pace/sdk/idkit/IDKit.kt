package cloud.pace.sdk.idkit

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import cloud.pace.sdk.utils.*
import cloud.pace.sdk.utils.Constants.TAG
import net.openid.appauth.*
import org.koin.core.inject

object IDKit : IDKitKoinComponent, LifecycleObserver {

    private val context: Context by inject()
    private val authorizationService: AuthorizationService by inject()
    private lateinit var configuration: OIDConfiguration
    private lateinit var authorizationRequest: AuthorizationRequest
    private lateinit var session: AuthState
    private var additionalCaching = true

    /**
     * Sets up [IDKit] with the passed [configuration].
     *
     * @param context The context.
     * @param additionalCaching If set to `false` persistent session cookies will be shared only natively.
     * If set to `true` the session will additionally be persisted by [IDKit] to improve the change of not having to resign in again. Defaults to `true`.
     */
    @JvmOverloads
    fun setup(context: Context, configuration: OIDConfiguration, additionalCaching: Boolean = true) {
        KoinConfig.setupIDKit(context)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        this.configuration = configuration
        this.additionalCaching = additionalCaching
        authorizationRequest = createAuthorizationRequest()

        if (additionalCaching) {
            SharedPreferenceUtils.loadSession(context)?.let {
                session = it
            }
        }
    }

    private fun createAuthorizationRequest(): AuthorizationRequest {
        val serviceConfiguration = AuthorizationServiceConfiguration(
            Uri.parse(configuration.authorizationEndpoint),
            Uri.parse(configuration.tokenEndpoint)
        )
        session = AuthState(serviceConfiguration)

        return AuthorizationRequest.Builder(serviceConfiguration, configuration.clientId, configuration.responseType, Uri.parse(configuration.redirectUri))
            .setPrompt("login")
            .setScopes(configuration.scopes)
            .setAdditionalParameters(configuration.additionalParameters)
            .build()
    }

    /**
     * Performs a discovery to retrieve a [ServiceConfiguration].
     *
     * @param issuerUri The issuer URI.
     * @param completion The block to be called when the discovery is complete either including the [ServiceConfiguration] or a [Throwable].
     */
    fun discoverConfiguration(issuerUri: String, completion: (Completion<ServiceConfiguration>) -> Unit) {
        AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(issuerUri)) { configuration, exception ->
            when {
                exception != null -> completion(Failure(exception))
                configuration != null -> {
                    completion(Success(ServiceConfiguration(configuration.authorizationEndpoint, configuration.tokenEndpoint, configuration.registrationEndpoint)))
                    Log.i(TAG, "Discovery successful")
                }
                else -> completion(Failure(FailedRetrievingConfigurationWhileDiscovering))
            }
        }
    }

    /**
     * Sends an authorization request to the authorization service, using a [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs).
     * Upon completion of this authorization request, a [PendingIntent] of the [completedActivity] will be invoked.
     * If the user cancels the authorization request, a [PendingIntent] of the [canceledActivity] will be invoked.
     *
     * Note: Call [handleAuthorizationResponse] in [completedActivity] or [canceledActivity] when the intent is returned from Chrome Custom Tab.
     */
    fun authorize(completedActivity: Class<*>, canceledActivity: Class<*>) {
        authorizationService.performAuthorizationRequest(
            authorizationRequest,
            PendingIntent.getActivity(context, 0, Intent(context, completedActivity), 0),
            PendingIntent.getActivity(context, 0, Intent(context, canceledActivity), 0)
        )
    }

    /**
     * Retrieves the currently authorized user's information.
     *
     * @param accessToken The user's access token for which to retrieve the user info.
     * @param completion The block to be called when the request is complete including either valid `userInfo` or a [Throwable].
     */
    fun userInfo(accessToken: String, completion: (Completion<UserInfoResponse>) -> Unit) {
        configuration.userInfoEndpoint.let {
            if (it != null) {
                UserInfoApiClient(it, accessToken).getUserInfo(completion)
            } else {
                completion(Failure(UserEndpointNotDefined))
            }
        }
    }

    /**
     * Creates an authorization request [Intent] that can be started with [android.app.Activity.startActivityForResult]
     * to open a [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs).
     *
     * Note: Call [handleAuthorizationResponse] in [android.app.Activity.onActivityResult] when the authorization result is returned from Chrome Custom Tab.
     */
    fun authorize(): Intent {
        return authorizationService.getAuthorizationRequestIntent(authorizationRequest)
    }

    /**
     * Sends a request to the authorization service to exchange a code granted as part of an authorization request for a token.
     *
     * @param intent Represents the [Intent] from the [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs).
     * @param completion The block to be called when the request is complete including either a valid `accessToken` or [Throwable].
     */
    fun handleAuthorizationResponse(intent: Intent, completion: (Completion<String?>) -> Unit) {
        val response = AuthorizationResponse.fromIntent(intent)
        val exception = AuthorizationException.fromIntent(intent)

        when {
            exception != null -> {
                session.update(response, exception)
                completion(Failure(exception))
            }
            response != null -> {
                session.update(response, exception)
                performTokenRequest(response.createTokenExchangeRequest(), completion)
            }
            else -> completion(Failure(FailedRetrievingSessionWhileAuthorizing))
        }
    }

    /**
     * Refreshes the current access token if needed.
     *
     * @param force Forces a refresh even if the current `accessToken` is still valid. Defaults to `false`.
     * @param completion The block to be called when the request is complete including either a new valid `accessToken` or a [Throwable].
     */
    @JvmOverloads
    fun refreshToken(force: Boolean = false, completion: (Completion<String?>) -> Unit) {
        if (isAuthorizationValid()) {
            if (force) {
                session.needsTokenRefresh = true
            }
            performTokenRequest(session.createTokenRefreshRequest(), completion)
        } else {
            completion(Failure(InvalidSession))
        }
    }

    private fun performTokenRequest(request: TokenRequest, completion: (Completion<String?>) -> Unit) {
        Log.i(TAG, "Trying to refresh token...")

        val clientSecret = configuration.clientSecret
        val clientAuthentication: ClientAuthentication = if (clientSecret != null) {
            ClientSecretBasic(clientSecret)
        } else {
            try {
                session.clientAuthentication
            } catch (e: ClientAuthentication.UnsupportedAuthenticationMethod) {
                Log.e(TAG, "Token request cannot be made, client authentication for the token endpoint could not be constructed", e)
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
            SharedPreferenceUtils.persistSession(context, session)
        }

        when {
            isAuthorizationValid() -> {
                completion(Success(session.accessToken))
                Log.i(TAG, "Token refresh successful")
            }
            exception != null -> {
                completion(Failure(exception))
            }
            else -> {
                completion(Failure(FailedRetrievingSessionWhileAuthorizing))
            }
        }
    }

    /**
     * Resets the current session.
     */
    fun resetSession() {
        val serviceConfiguration = session.authorizationServiceConfiguration
        if (serviceConfiguration != null) {
            val clearedState = AuthState(serviceConfiguration)
            val lastRegistrationResponse = session.lastRegistrationResponse
            if (lastRegistrationResponse != null) {
                clearedState.update(lastRegistrationResponse)
            }
            session = clearedState
        }
        SharedPreferenceUtils.persistSession(context, session)
    }

    /**
     * Checks the current authorization state. Returning `true` does not mean that the access is fresh - just that it was valid the last time it was used.
     *
     * @return The current state of the authorization.
     */
    fun isAuthorizationValid() = session.isAuthorized

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun dispose() {
        // This must be called to avoid memory leaks.
        authorizationService.dispose()
    }
}
