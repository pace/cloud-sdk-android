package cloud.pace.sdk.appkit

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import cloud.pace.sdk.BuildConfig
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.app.AppActivity
import cloud.pace.sdk.appkit.app.drawer.AppDrawer
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.model.Car
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.DeviceUtils
import cloud.pace.sdk.utils.Theme
import org.koin.core.inject

object AppKit : CloudSDKKoinComponent {

    private val appManager: AppManager by inject()
    internal lateinit var userAgent: String

    /**
     * Specifies whether the light or dark theme should be used for the apps.
     */
    var theme: Theme = Theme.LIGHT
        set(value) {
            field = value
            updateUserAgent()
        }

    /**
     * Specifies the minimum location accuracy in meters to request location based apps.
     */
    var locationAccuracy: Int? = null
        set(value) {
            field = value
            PACECloudSDK.setLocationAccuracy(value)
        }

    internal fun updateUserAgent() {
        val config = PACECloudSDK.configuration
        userAgent = listOf(
            "${config.clientAppName}/${config.clientAppVersion}_${config.clientAppBuild}",
            "(${DeviceUtils.getDeviceName()} Android/${DeviceUtils.getAndroidVersion()})",
            "PWA-SDK/${BuildConfig.VERSION_NAME}",
            if (config.clientId != null) "(clientid:${config.clientId};)" else "",
            if (theme == Theme.LIGHT) "PWASDK-Theme/Light" else "PWASDK-Theme/Dark",
            "IdentityManagement/${config.authenticationMode.value}",
            config.extensions.joinToString(" ")
        ).filter { it.isNotEmpty() }.joinToString(separator = " ")
    }

    /**
     * Checks for local based apps at the current location.
     *
     * @param completion Returns a list of [App]s on success or a [Throwable] on failure.
     */
    fun requestLocalApps(completion: (Completion<List<App>>) -> Unit) {
        appManager.requestLocalApps(completion)
    }

    /**
     * Returns a list of all apps.
     *
     * @param completion Returns a list of [App]s on success or a [Throwable] on failure.
     */
    fun requestApps(completion: (Completion<List<App>>) -> Unit) {
        appManager.requestApps(completion)
    }

    /**
     * Fetches the app with the given [url] and [references] (as PRNs/URNs).
     *
     * @param completion Returns a list of [App]s on success or a [Throwable] on failure.
     */
    fun fetchAppsByUrl(url: String, vararg references: String, completion: (Completion<List<App>>) -> Unit) {
        appManager.fetchAppsByUrl(url, references.toList(), completion)
    }

    /**
     * Fetches the app's URL for the given [appId] (not gas station ID).
     *
     * @param completion Returns the App's URL on success or a [Throwable] on failure.
     */
    fun fetchUrlByAppId(appId: String, completion: (Completion<String?>) -> Unit) {
        appManager.fetchUrlByAppId(appId, completion)
    }

    /**
     * Checks if there is at least one app for the given [poiId] at the current location.
     *
     * @param completion Completes with true if at least one app is available at the current location, otherwise false.
     */
    fun isPoiInRange(poiId: String, completion: (Boolean) -> Unit) {
        appManager.isPoiInRange(poiId, completion)
    }

    /**
     * Starts an [AppActivity] and loads the [url] of the app in the [cloud.pace.sdk.appkit.app.webview.AppWebView].
     *
     * @param context Context which should be used to start the [AppActivity].
     * @param enableBackToFinish True if the [AppActivity] should be finished or false if the [cloud.pace.sdk.appkit.app.webview.AppWebView] should navigate back on back press.
     * @param autoClose True if the [AppActivity] should be closed automatically when new apps are opened or the API does not return the app anymore, false otherwise.
     * @param callback Via this callback the client app can subscribe to certain app events.
     */
    @JvmOverloads
    fun openAppActivity(context: Context, url: String, enableBackToFinish: Boolean = false, autoClose: Boolean = true, callback: AppCallbackImpl? = null) {
        appManager.openAppActivity(context, url, enableBackToFinish, autoClose, callback)
    }

    /**
     * Starts an [AppActivity] and loads the URL of the [app] in the [cloud.pace.sdk.appkit.app.webview.AppWebView].
     *
     * @param context Context which should be used to start the [AppActivity].
     * @param enableBackToFinish True if the [AppActivity] should be finished or false if the [cloud.pace.sdk.appkit.app.webview.AppWebView] should navigate back on back press.
     * @param autoClose True if the [AppActivity] should be closed automatically when new apps are opened or the API does not return the app anymore, false otherwise.
     * @param callback Via this callback the client app can subscribe to certain app events.
     */
    @JvmOverloads
    fun openAppActivity(context: Context, app: App, enableBackToFinish: Boolean = false, autoClose: Boolean = true, callback: AppCallbackImpl? = null) {
        appManager.openAppActivity(context, app, enableBackToFinish, autoClose, callback)
    }

    /**
     * Adds an [AppDrawer] to the parent view [buttonContainer] for each app in [apps] list.
     * Clicking on the [AppDrawer] opens the [AppActivity] and shows the App.
     *
     * @param context Context which should be used to start the [AppActivity].
     * @param theme The [Theme] of the [AppDrawer].
     * @param bottomMargin The margin with which the [AppDrawer]s should be drawn to the bottom edge.
     * @param autoClose True if the [AppActivity] should be closed automatically when new apps are opened or no apps come back from the API, false otherwise.
     * @param callback Via this callback the client app can subscribe to certain app events.
     */
    @JvmOverloads
    fun openApps(
        context: Context,
        apps: List<App>,
        buttonContainer: ConstraintLayout,
        theme: Theme = Theme.LIGHT,
        bottomMargin: Float = 16f,
        autoClose: Boolean = true,
        callback: AppCallbackImpl? = null
    ) {
        appManager.openApps(context, apps, buttonContainer, theme, bottomMargin, autoClose, callback)
    }

    /**
     * Closes (removes) all [AppDrawer]s in the [buttonContainer] parent layout.
     * Also closes (finishes) the [AppActivity] if it was started with autoClose = true.
     *
     * @see openAppActivity
     * @see openApps
     */
    fun closeApps(buttonContainer: ConstraintLayout) {
        appManager.closeApps(buttonContainer)
    }

    /**
     * Closes (finishes) the [AppActivity], even if it was started with autoClose = false.
     *
     * @see openAppActivity
     * @see openApps
     */
    fun closeAppActivity() {
        appManager.closeAppActivity()
    }

    /**
     * Sets [car] related data which could be needed by an App.
     */
    fun setCarData(car: Car) {
        appManager.setCarData(car)
    }
}
