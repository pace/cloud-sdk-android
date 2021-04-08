package cloud.pace.sdk.idkit.authorization

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import cloud.pace.sdk.idkit.model.*
import cloud.pace.sdk.idkit.userinfo.UserInfoApiClient
import cloud.pace.sdk.idkit.userinfo.UserInfoResponse
import cloud.pace.sdk.utils.*
import net.openid.appauth.*

internal class AuthorizationManager(
    private val context: Context,
    private val authorizationService: AuthorizationService
) : CloudSDKKoinComponent, LifecycleObserver {

    private lateinit var configuration: OIDConfiguration
    private lateinit var authorizationRequest: AuthorizationRequest
    private lateinit var session: AuthState

    private var additionalCaching = true

    internal fun setup(context: Context, configuration: OIDConfiguration, additionalCaching: Boolean = true) {
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

    internal fun discoverConfiguration(issuerUri: String, completion: (Completion<ServiceConfiguration>) -> Unit) {
        AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(issuerUri)) { configuration, exception ->
            when {
                exception != null -> completion(
                    Failure(
                        AuthorizationError(exception.type, exception.code, exception.error, exception.errorDescription, exception.errorUri, exception.message, exception.cause)
                    )
                )
                configuration != null -> {
                    completion(Success(ServiceConfiguration(configuration.authorizationEndpoint, configuration.tokenEndpoint, configuration.endSessionEndpoint, configuration.registrationEndpoint)))
                    Log.i(Constants.TAG, "Discovery successful")
                }
                else -> completion(Failure(FailedRetrievingConfigurationWhileDiscovering))
            }
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

    internal fun handleEndSessionResponse(intent: Intent, completion: (Completion<Unit>) -> Unit) {
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
                completion(Failure(UserEndpointNotDefined))
            }
        }
    }

    private fun createAuthorizationRequest(): AuthorizationRequest {
        val serviceConfiguration = getAuthorizationServiceConfiguration()
        session = AuthState(serviceConfiguration)

        return AuthorizationRequest.Builder(serviceConfiguration, configuration.clientId, configuration.responseType, Uri.parse(configuration.redirectUri))
            .setPrompt("login") // Disable SSO --> Always show login screen
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
        Log.i(Constants.TAG, "Trying to refresh token...")

        val clientSecret = configuration.clientSecret
        val clientAuthentication: ClientAuthentication = if (clientSecret != null) {
            ClientSecretBasic(clientSecret)
        } else {
            try {
                session.clientAuthentication
            } catch (e: ClientAuthentication.UnsupportedAuthenticationMethod) {
                Log.e(Constants.TAG, "Token request cannot be made, client authentication for the token endpoint could not be constructed", e)
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
                Log.i(Constants.TAG, "Token refresh successful")
            }
            else -> {
                completion(Failure(FailedRetrievingSessionWhileAuthorizing))
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun dispose() {
        // This must be called to avoid memory leaks.
        authorizationService.dispose()
    }
}