package cloud.pace.sdk.appkit.communication

import android.content.Context
import android.graphics.Bitmap
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Success
import org.koin.core.inject

/**
 * Public callback functions to subscribe to app events.
 */
interface AppCallback {

    /**
     * Is called when the app was opened (e.g. by clicking the [cloud.pace.sdk.appkit.app.drawer.AppDrawer]).
     *
     * @param app The app that was opened or null if no app is available (e.g. if the app was opened by URL).
     */
    fun onOpen(app: App?)

    /**
     * Is called when the app needs to get closed (e.g. when it is not available anymore).
     */
    fun onClose()

    /**
     * Is called when the app sends the open URL in new tab action.
     */
    fun onOpenInNewTab(url: String)

    /**
     * Is called when the app sends the disable action.
     *
     * @param host The host that was disabled.
     */
    fun onDisable(host: String)

    /**
     * Is called when the app sends the access token is invalid action.
     * The client app needs to call the [onResult] function to set the [GetAccessTokenResponse].
     *
     * @param reason Specifies the reason why the token is invalid. [InvalidTokenReason.UNAUTHORIZED] means that the session had been invalidated
     * and [InvalidTokenReason.OTHER] if the token has expired and should be renewed via [onResult].
     * @param oldToken The last access token.
     */
    fun getAccessToken(reason: InvalidTokenReason, oldToken: String?, onResult: (GetAccessTokenResponse) -> Unit)

    /**
     * Is called if an automatic session renewal triggered by the SDK itself fails.
     * The client app needs to call the [onResult] function to set a new access token or null in case of error.
     *
     * Implement this method to specify a custom behavior for the token retrieval.
     * If not implemented an authorization will be performed automatically which will result in showing a sign in mask for the user.
     *
     * @param throwable The error that caused the session renewal to fail if available.
     */
    fun onSessionRenewalFailed(throwable: Throwable?, onResult: (String?) -> Unit)

    /**
     * Is called when the user logs in via an automatic authorization request from the SDK within the PWA.
     * This callback can be used to display an [AlertDialog][androidx.appcompat.app.AlertDialog] after an authorization within the PWA, for which the [AppActivity][cloud.pace.sdk.appkit.app.AppActivity] context is needed.
     *
     * @param context The [AppActivity][cloud.pace.sdk.appkit.app.AppActivity] context.
     * @param result A valid `accessToken` or [Throwable] in case of error.
     */
    fun onLogin(context: Context, result: Completion<String?>)

    /**
     * Is called when the app sends a request to logout the current user.
     * The client app needs to call the [onResult] function to set the [LogoutResponse].
     * Use [LogoutResponse.SUCCESSFUL] to signal a successful logout, [LogoutResponse.UNAUTHORIZED] if the user was not logged in at all and [LogoutResponse.OTHER] for all other errors.
     */
    fun onLogout(onResult: (LogoutResponse) -> Unit)

    /**
     * Is called when the client app hasn't set up deep linking via a custom scheme,
     * which the app is trying to trigger, and passes the [scheme] for which it failed.
     *
     * @param context The [cloud.pace.sdk.appkit.app.AppActivity] context.
     */
    fun onCustomSchemeError(context: Context?, scheme: String)

    /**
     * Is called when the app sends an image.
     *
     * @param bitmap The image as bitmap.
     */
    fun onImageDataReceived(bitmap: Bitmap)

    /**
     * Is called when the app sends a user property.
     *
     * @param key The key of the user property
     * @param value The value of the user property
     * @param update Whether the user property should be updated or not
     */
    fun setUserProperty(key: String, value: String, update: Boolean)

    /**
     * Is called when the app sends an event to be logged.
     *
     * @param key The event key
     * @param parameters Optional parameters to be logged
     */
    fun logEvent(key: String, parameters: Map<String, Any>)

    /**
     * Is called when the app requests a configuration.
     *
     * @param key The configuration key
     * @param config Call this function to pass the configuration or `null` if none is available
     */
    fun getConfig(key: String, config: (String?) -> Unit)
}

abstract class AppCallbackImpl : AppCallback, CloudSDKKoinComponent {

    private val appModel: AppModel by inject()

    override fun onOpen(app: App?) {}
    override fun onClose() {}
    override fun onOpenInNewTab(url: String) {}
    override fun onDisable(host: String) {}
    override fun getAccessToken(reason: InvalidTokenReason, oldToken: String?, onResult: (GetAccessTokenResponse) -> Unit) {}
    override fun onSessionRenewalFailed(throwable: Throwable?, onResult: (String?) -> Unit) {
        appModel.authorize {
            onResult((it as? Success)?.result)
        }
    }

    override fun onLogin(context: Context, result: Completion<String?>) {}
    override fun onLogout(onResult: (LogoutResponse) -> Unit) {
        if (IDKit.isInitialized) {
            appModel.endSession(onResult)
        }
    }

    override fun onCustomSchemeError(context: Context?, scheme: String) {}
    override fun onImageDataReceived(bitmap: Bitmap) {}
    override fun setUserProperty(key: String, value: String, update: Boolean) {}
    override fun logEvent(key: String, parameters: Map<String, Any>) {}
    override fun getConfig(key: String, config: (String?) -> Unit) {
        config(null)
    }
}
