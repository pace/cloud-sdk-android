package cloud.pace.sdk.appkit.communication

import android.content.Context
import android.graphics.Bitmap
import cloud.pace.sdk.appkit.app.drawer.ui.AppDrawer
import cloud.pace.sdk.appkit.app.drawer.ui.AppDrawerHost
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Success
import org.koin.core.component.inject

/**
 * Public callback functions to subscribe to app events.
 */
interface AppCallback {

    /**
     * Is called when the [AppDrawerHost] shows a list of [App]s.
     * This callback is triggered when the apps or their attributes change.
     *
     * @param apps The list of [App]s, which are shown as [AppDrawer]s from the [AppDrawerHost].
     */
    fun onShow(apps: List<App>)

    /**
     * Is called when the app was opened (e.g. by clicking the [cloud.pace.sdk.appkit.app.drawer.ui.AppDrawer]).
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
     * If it is not overwritten, it opens the share sheet to share the image.
     *
     * @param bitmap The image as bitmap.
     */
    fun onImageDataReceived(bitmap: Bitmap)

    /**
     * Is called when the app sends a file, which can currently only be a PDF.
     * If it is not overwritten, it opens the share sheet to share the file.
     *
     * @param fileData The file data as byte array.
     */
    fun onFileDataReceived(fileData: ByteArray)

    /**
     * Is called when the app sends a text to share.
     * If it is not overwritten, it opens the system share sheet.
     *
     * @param text The text that will be shown.
     * @param title The title that will shown
     */
    fun onShareTextReceived(text: String, title: String)

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

    /**
     * Is called whenever the current app tries to redirect to another specified [app].
     * The client app can then decide, if this should be allowed (set [isAllowed] to `true`) or if the client intercepts the app (set [isAllowed] to `false`),
     * e.g. native apps with a map probably don't want to show the fuel station finder, but show their own map instead.
     *
     * @param app The app the current app is redirecting to, e.g. `fuel-station-finder`, `fueling`, `pay`, or `legal`
     * @param isAllowed Call this function to specify whether to redirect or not (defaults to `true`)
     */
    fun isAppRedirectAllowed(app: String, isAllowed: (Boolean) -> Unit)

    /**
     * Is called when the app wants to know if the user is potentially signed in without triggering an actual sign in action.
     *
     * @param isSignedIn Call this function to specify whether the user is signed in or not
     */
    fun isSignedIn(isSignedIn: (Boolean) -> Unit)

    /**
     * Is called when the app wants to know if the remote config feature is generally available.
     *
     * @param isAvailable Call this function to specify whether the remote config feature is available or not
     */
    fun isRemoteConfigAvailable(isAvailable: (Boolean) -> Unit)

    /**
     * Is called when the app receives a navigation request to the specified location.
     * If it is not overwritten, it starts the navigation in the navigation app selected by the user.
     *
     * @param lat The latitude of the location to be navigated to.
     * @param lon The longitude of the location to be navigated to.
     * @param name The name of the location.
     */
    fun onNavigationRequestReceived(lat: Double, lon: Double, name: String)

    /**
     * Is called when the app asks for an additional email to send the receipt to besides the user's email
     *
     * @param paymentMethod For which payment method the email is requested
     * @param email Call this function to specify an email or not
     */
    fun onReceiptEmailRequestReceived(paymentMethod: String, email: (String?) -> Unit)

    /**
     * Is called when the app asks for additional attachments that should be printed on the fueling receipt
     *
     * @param paymentMethod For which payment method the attachments are requested
     * @param attachments Call this function to specify attachments or not
     */
    fun onReceiptAttachmentsRequestReceived(paymentMethod: String, attachments: (List<String>?) -> Unit)
}

abstract class AppCallbackImpl : AppCallback, CloudSDKKoinComponent {

    private val appModel: AppModel by inject()

    override fun onShow(apps: List<App>) {}
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
    override fun onImageDataReceived(bitmap: Bitmap) {
        appModel.showShareSheet(bitmap)
    }

    override fun onFileDataReceived(fileData: ByteArray) {
        appModel.showShareSheet(fileData)
    }

    override fun onShareTextReceived(text: String, title: String) {
        appModel.showShareSheet(text, title)
    }

    override fun setUserProperty(key: String, value: String, update: Boolean) {}
    override fun logEvent(key: String, parameters: Map<String, Any>) {}
    override fun getConfig(key: String, config: (String?) -> Unit) {
        config(null)
    }

    override fun isAppRedirectAllowed(app: String, isAllowed: (Boolean) -> Unit) {
        isAllowed(true)
    }

    override fun isSignedIn(isSignedIn: (Boolean) -> Unit) {
        isSignedIn(IDKit.isInitialized && IDKit.isAuthorizationValid())
    }

    override fun isRemoteConfigAvailable(isAvailable: (Boolean) -> Unit) {
        isAvailable(false)
    }

    override fun onNavigationRequestReceived(lat: Double, lon: Double, name: String) {
        appModel.startNavigation(lat, lon)
    }

    override fun onReceiptEmailRequestReceived(paymentMethod: String, email: (String?) -> Unit) {
        email(null)
    }

    override fun onReceiptAttachmentsRequestReceived(paymentMethod: String, attachments: (List<String>?) -> Unit) {
        attachments(null)
    }
}
