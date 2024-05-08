package cloud.pace.sdk.idkit.authorization

import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.R
import cloud.pace.sdk.api.API
import cloud.pace.sdk.idkit.authorization.integrated.AuthorizationWebViewActivity
import cloud.pace.sdk.idkit.model.FailedRetrievingConfigurationWhileDiscovering
import cloud.pace.sdk.idkit.model.FailedRetrievingSessionWhileAuthorizing
import cloud.pace.sdk.idkit.model.FailedRetrievingSessionWhileEnding
import cloud.pace.sdk.idkit.model.InternalError
import cloud.pace.sdk.idkit.model.InvalidSession
import cloud.pace.sdk.idkit.model.NoSupportedBrowser
import cloud.pace.sdk.idkit.model.OIDConfiguration
import cloud.pace.sdk.idkit.model.OperationCanceled
import cloud.pace.sdk.idkit.model.ServiceConfiguration
import cloud.pace.sdk.idkit.model.toAuthorizationServiceConfiguration
import cloud.pace.sdk.idkit.userinfo.UserInfoApiClient
import cloud.pace.sdk.idkit.userinfo.UserInfoResponse
import cloud.pace.sdk.utils.Canceled
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.IntentResult
import cloud.pace.sdk.utils.Ok
import cloud.pace.sdk.utils.SetupLogger
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.getResultFor
import cloud.pace.sdk.utils.resumeIfActive
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.EndSessionRequest
import net.openid.appauth.EndSessionResponse
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import timber.log.Timber

internal class AuthorizationManager(
    private val context: Context,
    private val authorizationService: AuthorizationService,
    private val sessionHolder: SessionHolder,
    private val userInfoApi: UserInfoApiClient
) : CloudSDKKoinComponent, DefaultLifecycleObserver {

    private lateinit var clientId: String
    private lateinit var configuration: OIDConfiguration
    private lateinit var authorizationRequest: AuthorizationRequest

    internal fun setup(clientId: String, configuration: OIDConfiguration) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        this.clientId = clientId
        this.configuration = configuration.apply {
            additionalParameters = getMergedParameters(additionalParameters ?: emptyMap())
        }

        createAuthorizationRequest()
        sessionHolder.updateSession(configuration)
    }

    internal fun setAdditionalParameters(params: Map<String, String>?) {
        if (::configuration.isInitialized) {
            configuration.additionalParameters = getMergedParameters(params ?: emptyMap())
            createAuthorizationRequest()
        } else {
            SetupLogger.logSDKWarningIfNeeded()
        }
    }

    internal fun getAdditionalParameters(): Map<String, String>? {
        return if (::configuration.isInitialized) {
            configuration.additionalParameters
        } else {
            SetupLogger.logSDKWarningIfNeeded()
            null
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
                    Timber.w(throwable, "Failed to discover configuration")
                    completion(Failure(throwable))
                }
            }
        }
    }

    internal suspend fun authorize(activity: AppCompatActivity): Completion<String?> {
        return when (val authorizationRequest = authorize()) {
            is Success -> handleAuthorizationResult(activity.getResultFor(authorizationRequest.result))
            is Failure -> Failure(authorizationRequest.throwable)
        }
    }

    internal suspend fun authorize(fragment: Fragment): Completion<String?> {
        return when (val authorizationRequest = authorize()) {
            is Success -> handleAuthorizationResult(fragment.getResultFor(authorizationRequest.result))
            is Failure -> Failure(authorizationRequest.throwable)
        }
    }

    internal fun authorize(completedActivity: Class<*>, canceledActivity: Class<*>) {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        if (configuration.integrated) {
            val intent = AuthorizationWebViewActivity.createStartIntent(
                context,
                authorizationRequest,
                PendingIntent.getActivity(context, 0, Intent(context, completedActivity), flags),
                PendingIntent.getActivity(context, 0, Intent(context, canceledActivity), flags)
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            val canceledPendingIntent = PendingIntent.getActivity(context, 0, Intent(context, canceledActivity), flags)

            try {
                authorizationService.performAuthorizationRequest(
                    authorizationRequest,
                    PendingIntent.getActivity(context, 0, Intent(context, completedActivity), flags),
                    canceledPendingIntent
                )
            } catch (e: ActivityNotFoundException) {
                Timber.i(e, "No supported browser installed to launch the authorization request")
                sendCanceledPendingIntent(canceledPendingIntent)
            }
        }
    }

    internal fun authorize(): Completion<Intent> {
        return if (configuration.integrated) {
            Success(AuthorizationWebViewActivity.createStartIntent(context, authorizationRequest))
        } else {
            try {
                Success(authorizationService.getAuthorizationRequestIntent(authorizationRequest))
            } catch (e: ActivityNotFoundException) {
                Timber.i(e, "No supported browser installed to launch the authorization request")
                showNoSupportedBrowserToast()
                Failure(NoSupportedBrowser)
            }
        }
    }

    private suspend fun handleAuthorizationResult(result: IntentResult): Completion<String?> {
        return when (result) {
            is Ok -> result.data?.let { intent ->
                suspendCancellableCoroutine { continuation ->
                    handleAuthorizationResponse(intent) {
                        continuation.resumeIfActive(it)
                    }
                }
            } ?: Failure(InternalError)

            is Canceled -> Failure(OperationCanceled)
        }
    }

    internal fun handleAuthorizationResponse(intent: Intent, completion: (Completion<String?>) -> Unit) {
        val response = AuthorizationResponse.fromIntent(intent)
        val exception = AuthorizationException.fromIntent(intent)

        when {
            exception != null -> {
                sessionHolder.session?.update(response, exception)
                Timber.e(exception, "Failed to handle authorization response")
                completion(Failure(exception))
            }

            response != null -> {
                sessionHolder.session?.update(response, exception)
                performTokenRequest(response.createTokenExchangeRequest(), completion)
            }

            else -> {
                val throwable = FailedRetrievingSessionWhileAuthorizing
                Timber.w(throwable, "Failed to handle authorization response")
                completion(Failure(throwable))
            }
        }
    }

    internal fun refreshToken(force: Boolean = false, completion: (Completion<String?>) -> Unit) {
        if (isAuthorizationValid()) {
            if (force) {
                sessionHolder.session?.needsTokenRefresh = true
            }
            sessionHolder.session?.createTokenRefreshRequest()?.let { performTokenRequest(it, completion) } ?: completion(Failure(InvalidSession))
        } else {
            completion(Failure(InvalidSession))
        }
    }

    internal suspend fun endSession(activity: AppCompatActivity): Completion<Unit> {
        return when (val endSessionRequest = endSession()) {
            is Success -> handleEndSessionResult(activity.getResultFor(endSessionRequest.result))
            is Failure -> Failure(endSessionRequest.throwable)
        }
    }

    internal suspend fun endSession(fragment: Fragment): Completion<Unit> {
        return when (val endSessionRequest = endSession()) {
            is Success -> handleEndSessionResult(fragment.getResultFor(endSessionRequest.result))
            is Failure -> Failure(endSessionRequest.throwable)
        }
    }

    internal fun endSession(completedActivity: Class<*>, canceledActivity: Class<*>): Boolean {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return createEndSessionRequest()?.let {
            if (configuration.integrated) {
                val intent = AuthorizationWebViewActivity.createStartIntent(
                    context,
                    it,
                    PendingIntent.getActivity(context, 0, Intent(context, completedActivity), flags),
                    PendingIntent.getActivity(context, 0, Intent(context, canceledActivity), flags)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                val canceledPendingIntent = PendingIntent.getActivity(context, 0, Intent(context, canceledActivity), flags)

                try {
                    authorizationService.performEndSessionRequest(
                        it,
                        PendingIntent.getActivity(context, 0, Intent(context, completedActivity), flags),
                        canceledPendingIntent
                    )
                } catch (e: ActivityNotFoundException) {
                    Timber.i(e, "No supported browser installed to launch the end session request")
                    sendCanceledPendingIntent(canceledPendingIntent)
                }
            }
            true
        } ?: false
    }

    internal fun endSession(): Completion<Intent> {
        val endSessionRequest = createEndSessionRequest() ?: return Failure(FailedRetrievingSessionWhileEnding)

        return if (configuration.integrated) {
            Success(AuthorizationWebViewActivity.createStartIntent(context, endSessionRequest))
        } else {
            try {
                Success(authorizationService.getEndSessionRequestIntent(endSessionRequest))
            } catch (e: ActivityNotFoundException) {
                Timber.i(e, "No supported browser installed to launch the end session request")
                showNoSupportedBrowserToast()
                Failure(NoSupportedBrowser)
            }
        }
    }

    private suspend fun handleEndSessionResult(result: IntentResult): Completion<Unit> {
        return when (result) {
            is Ok -> result.data?.let { intent ->
                suspendCancellableCoroutine { continuation ->
                    handleEndSessionResponse(intent) {
                        continuation.resumeIfActive(it)
                    }
                }
            } ?: Failure(InternalError)

            is Canceled -> Failure(OperationCanceled)
        }
    }

    internal fun handleEndSessionResponse(intent: Intent, completion: (Completion<Unit>) -> Unit) {
        val response = EndSessionResponse.fromIntent(intent)
        val exception = AuthorizationException.fromIntent(intent)

        when {
            exception != null -> {
                Timber.e(exception, "Failed to handle end session response")
                completion(Failure(exception))
            }

            response != null -> {
                API.addAuthorizationHeader(null)
                sessionHolder.clearSessionAndPreferences()
                completion(Success(Unit))
            }

            else -> {
                val throwable = FailedRetrievingSessionWhileEnding
                Timber.w(throwable, "Failed to handle end session response")
                completion(Failure(throwable))
            }
        }
    }

    internal fun isAuthorizationValid() = sessionHolder.isAuthorizationValid()

    internal fun containsAuthorizationResponse(intent: Intent) = intent.hasExtra(AuthorizationResponse.EXTRA_RESPONSE)

    internal fun containsEndSessionResponse(intent: Intent) = intent.hasExtra(EndSessionResponse.EXTRA_RESPONSE)

    internal fun containsException(intent: Intent) = intent.hasExtra(AuthorizationException.EXTRA_EXCEPTION)

    internal fun cachedToken() = sessionHolder.cachedToken()

    internal fun userInfo(additionalHeaders: Map<String, String>? = null, additionalParameters: Map<String, String>? = null, completion: (Completion<UserInfoResponse>) -> Unit) {
        userInfoApi.getUserInfo(additionalHeaders, additionalParameters, completion)
    }

    private fun getMergedParameters(idKitParams: Map<String, String>): Map<String, String> {
        // If there are keys in both param maps, they will be overwritten with the values from PACECloudSDK.additionalQueryParams
        return idKitParams + PACECloudSDK.additionalQueryParams
    }

    private fun createAuthorizationRequest() {
        val serviceConfiguration = configuration.toAuthorizationServiceConfiguration()
        authorizationRequest = AuthorizationRequest.Builder(serviceConfiguration, clientId, configuration.responseType, Uri.parse(configuration.redirectUri))
            .setScopes(configuration.scopes?.plus("openid") ?: listOf("openid")) // Make sure that 'openid' is passed as scope so that the idToken for the end session request is returned
            .setAdditionalParameters(configuration.additionalParameters)
            .setPrompt(AuthorizationRequest.Prompt.LOGIN)
            .build()
    }

    private fun createEndSessionRequest(): EndSessionRequest? {
        return sessionHolder.session?.idToken?.let {
            EndSessionRequest.Builder(configuration.toAuthorizationServiceConfiguration())
                .setIdTokenHint(it)
                .setPostLogoutRedirectUri(Uri.parse(configuration.redirectUri))
                .build()
        }
    }

    private fun performTokenRequest(request: TokenRequest, completion: (Completion<String?>) -> Unit) {
        Timber.i("Trying to refresh token...")

        val clientSecret = configuration.clientSecret
        val clientAuthentication = if (clientSecret != null) {
            ClientSecretBasic(clientSecret)
        } else {
            try {
                sessionHolder.session?.clientAuthentication
            } catch (e: ClientAuthentication.UnsupportedAuthenticationMethod) {
                Timber.e(e, "Token request cannot be made, client authentication for the token endpoint could not be constructed")
                completion(Failure(e))
                return
            }
        }

        if (clientAuthentication != null) {
            authorizationService.performTokenRequest(request, clientAuthentication) { tokenResponse, exception ->
                handleTokenResponse(tokenResponse, exception, completion)
            }
        } else {
            completion(Failure(InvalidSession))
        }
    }

    private fun handleTokenResponse(tokenResponse: TokenResponse?, exception: AuthorizationException?, completion: (Completion<String?>) -> Unit) {
        sessionHolder.session?.update(tokenResponse, exception)
        sessionHolder.persistSession()

        when {
            exception != null -> {
                Timber.e(exception, "Failed to handle token response")
                completion(Failure(exception))
            }

            tokenResponse != null -> {
                val accessToken = sessionHolder.cachedToken()
                accessToken?.let { API.addAuthorizationHeader(it) }
                completion(Success(accessToken))
                Timber.i("Token refresh successful")
            }

            else -> {
                val throwable = FailedRetrievingSessionWhileAuthorizing
                Timber.w(throwable, "Failed to handle token response")
                completion(Failure(throwable))
            }
        }
    }

    private fun showNoSupportedBrowserToast() {
        Toast.makeText(context, R.string.no_supported_browser_toast, Toast.LENGTH_LONG).show()
    }

    private fun sendCanceledPendingIntent(canceledPendingIntent: PendingIntent) {
        showNoSupportedBrowserToast()
        val cancelData = AuthorizationException.fromTemplate(AuthorizationException.GeneralErrors.PROGRAM_CANCELED_AUTH_FLOW, null).toIntent()

        try {
            canceledPendingIntent.send(context, 0, cancelData)
        } catch (e: CanceledException) {
            Timber.e(e, "Failed to send cancel intent")
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        // This must be called to avoid memory leaks.
        authorizationService.dispose()
    }
}
