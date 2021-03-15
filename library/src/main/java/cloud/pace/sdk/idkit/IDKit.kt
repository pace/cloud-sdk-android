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

    /**
     * Performs a discovery to retrieve a [ServiceConfiguration].
     *
     * @param issuerUri The issuer URI.
     * @param completion The block to be called when the discovery is complete either including the [ServiceConfiguration] or a [Throwable].
     */
    fun discoverConfiguration(issuerUri: String, completion: (Completion<ServiceConfiguration>) -> Unit) {
        AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(issuerUri)) { configuration, exception ->
            when {
                exception != null -> completion(
                    Failure(
                        AuthorizationError(exception.type, exception.code, exception.error, exception.errorDescription, exception.errorUri, exception.message, exception.cause)
                    )
                )
                configuration != null -> {
                    completion(Success(ServiceConfiguration(configuration.authorizationEndpoint, configuration.tokenEndpoint, configuration.endSessionEndpoint, configuration.registrationEndpoint)))
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
     * Creates an authorization request [Intent] that can be launched using the Activity Result API and a StartActivityForResult contract or with [android.app.Activity.startActivityForResult]
     * to open a [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs).
     *
     * Note: Call [handleAuthorizationResponse] in ActivityResultCallback if Activity Result API was used or in [android.app.Activity.onActivityResult] if
     * [android.app.Activity.startActivityForResult] was used when the authorization result is returned from Chrome Custom Tab.
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
                completion(
                    Failure(
                        AuthorizationError(exception.type, exception.code, exception.error, exception.errorDescription, exception.errorUri, exception.message, exception.cause)
                    )
                )
            }
            response != null -> {
                session.update(response, exception)
                performTokenRequest(response.createTokenExchangeRequest(), completion)
            }
            else -> completion(Failure(FailedRetrievingSessionWhileAuthorizing))
        }
    }

    /**
     * Resets the local session object.
     *
     * @param intent Represents the [Intent] from the [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs).
     * @param completion The block to be called when the session was reset or a [Throwable] when an exception occurred.
     */
    fun handleEndSessionResponse(intent: Intent, completion: (Completion<Unit>) -> Unit) {
        val response = EndSessionResponse.fromIntent(intent)
        val exception = AuthorizationException.fromIntent(intent)

        when {
            exception != null -> completion(Failure(exception))
            response != null -> {
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

                completion(Success(Unit))
            }
            else -> completion(Failure(FailedRetrievingSessionWhileEnding))
        }
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

    /**
     * Sends an end session request to the authorization service, using a [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs).
     * Upon completion of this end session request, a [PendingIntent] of the [completedActivity] will be invoked.
     * If the user cancels the end session request, a [PendingIntent] of the [canceledActivity] will be invoked.
     *
     * Note: Call [handleEndSessionResponse] in [completedActivity] or [canceledActivity] when the intent is returned from Chrome Custom Tab.
     *
     * @return True if an end session request could be performed, false otherwise (due to an invalid session).
     */
    fun endSession(completedActivity: Class<*>, canceledActivity: Class<*>): Boolean {
        return createEndSessionRequest()?.let {
            authorizationService.performEndSessionRequest(
                it,
                PendingIntent.getActivity(context, 0, Intent(context, completedActivity), 0),
                PendingIntent.getActivity(context, 0, Intent(context, canceledActivity), 0)
            )
            true
        } ?: false
    }

    /**
     * Creates an end session request [Intent] that can be launched using the Activity Result API and a StartActivityForResult contract or with [android.app.Activity.startActivityForResult]
     * to open a [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs).
     *
     * Note: Call [handleEndSessionResponse] in ActivityResultCallback if Activity Result API was used or in [android.app.Activity.onActivityResult] if
     * [android.app.Activity.startActivityForResult] was used when the end session result is returned from Chrome Custom Tab.
     *
     * @return The end session request [Intent] or null, if none could be created due to an invalid session.
     */
    fun endSession(): Intent? {
        return createEndSessionRequest()?.let { authorizationService.getEndSessionRequestIntent(it) }
    }

    /**
     * Checks the current authorization state. Returning `true` does not mean that the access is fresh - just that it was valid the last time it was used.
     *
     * @return The current state of the authorization.
     */
    fun isAuthorizationValid() = session.isAuthorized

    /**
     * Checks if the [intent] contains an authorization response.
     *
     * @return True if the [intent] has an authorization response extra, false otherwise.
     */
    fun containsAuthorizationResponse(intent: Intent) = intent.hasExtra(AuthorizationResponse.EXTRA_RESPONSE)

    /**
     * Checks if the [intent] contains an end session response.
     *
     * @return True if the [intent] has an end session response extra, false otherwise.
     */
    fun containsEndSessionResponse(intent: Intent) = intent.hasExtra(EndSessionResponse.EXTRA_RESPONSE)

    /**
     * Checks if the [intent] contains an authorization/end session exception.
     *
     * @return True if the [intent] has an exception extra, false otherwise.
     */
    fun containsException(intent: Intent) = intent.hasExtra(AuthorizationException.EXTRA_EXCEPTION)

    /**
     * Returns the cached accessToken of the current [session] or null if the session is not initialized or has no accessToken.
     */
    fun cachedToken() = if (::session.isInitialized) session.accessToken else null

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun dispose() {
        // This must be called to avoid memory leaks.
        authorizationService.dispose()
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
            exception != null -> {
                completion(
                    Failure(
                        AuthorizationError(exception.type, exception.code, exception.error, exception.errorDescription, exception.errorUri, exception.message, exception.cause)
                    )
                )
            }
            tokenResponse != null -> {
                completion(Success(session.accessToken))
                Log.i(TAG, "Token refresh successful")
            }
            else -> {
                completion(Failure(FailedRetrievingSessionWhileAuthorizing))
            }
        }
    }
}
