package cloud.pace.sdk.appkit.communication

import android.content.Context
import android.graphics.Bitmap
import cloud.pace.sdk.appkit.model.App

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
     * The client app needs to call the [onResult] function to set a new access token.
     */
    fun onTokenInvalid(onResult: (String) -> Unit)

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
}

abstract class AppCallbackImpl : AppCallback {

    override fun onOpen(app: App?) {}
    override fun onClose() {}
    override fun onOpenInNewTab(url: String) {}
    override fun onDisable(host: String) {}
    override fun onTokenInvalid(onResult: (String) -> Unit) {}
    override fun onCustomSchemeError(context: Context?, scheme: String) {}
    override fun onImageDataReceived(bitmap: Bitmap) {}
}
