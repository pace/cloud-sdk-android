package cloud.pace.sdk.appkit

import android.content.Context
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.app.AppActivity
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.model.Car
import cloud.pace.sdk.utils.*
import org.koin.core.component.inject

object AppKit : CloudSDKKoinComponent {

    private val appManager: AppManager by inject()
    internal lateinit var userAgent: String
    internal val defaultAppCallback = object : AppCallbackImpl() {}

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

    /**
     * Checks whether [PACECloudSDK] has been set up correctly before [AppKit] is used, otherwise log SDK warnings.
     */
    init {
        SetupLogger.logSDKWarningIfNeeded()
    }

    internal fun updateUserAgent() {
        val config = PACECloudSDK.configuration
        userAgent = listOf(
            PACECloudSDK.getBaseUserAgent(),
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
     * Fetches the app with the given [url] and [references] (e.g. referenced gas station UUIDs).
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
     * Starts an [AppActivity] and loads the [url] of the app in the [cloud.pace.sdk.appkit.app.webview.AppWebView].
     *
     * @param context Context which should be used to start the [AppActivity].
     * @param enableBackToFinish True if the [AppActivity] should be finished or false if the [cloud.pace.sdk.appkit.app.webview.AppWebView] should navigate back on back press.
     * @param callback Via this callback the client app can subscribe to certain app events.
     */
    @JvmOverloads
    fun openAppActivity(context: Context, url: String, enableBackToFinish: Boolean = false, callback: AppCallbackImpl = defaultAppCallback) {
        appManager.openAppActivity(context, url, theme, enableBackToFinish, callback)
    }

    /**
     * Starts an [AppActivity] and loads the URL of the [app] in the [cloud.pace.sdk.appkit.app.webview.AppWebView].
     *
     * @param context Context which should be used to start the [AppActivity].
     * @param enableBackToFinish True if the [AppActivity] should be finished or false if the [cloud.pace.sdk.appkit.app.webview.AppWebView] should navigate back on back press.
     * @param callback Via this callback the client app can subscribe to certain app events.
     */
    @JvmOverloads
    fun openAppActivity(context: Context, app: App, enableBackToFinish: Boolean = false, callback: AppCallbackImpl = defaultAppCallback) {
        appManager.openAppActivity(context, app, theme, enableBackToFinish, callback)
    }

    /**
     * Starts an [AppActivity] and loads PaceID in the [cloud.pace.sdk.appkit.app.webview.AppWebView].
     *
     * @param context Context which should be used to start the [AppActivity].
     * @param enableBackToFinish True if the [AppActivity] should be finished or false if the [cloud.pace.sdk.appkit.app.webview.AppWebView] should navigate back on back press.
     * @param callback Via this callback the client app can subscribe to certain app events.
     */
    @JvmOverloads
    fun openPaceID(context: Context, enableBackToFinish: Boolean = true, callback: AppCallbackImpl = defaultAppCallback) {
        appManager.openAppActivity(context, URL.paceID, theme, enableBackToFinish, callback)
    }

    /**
     * Starts an [AppActivity] and loads the Payment App in the [cloud.pace.sdk.appkit.app.webview.AppWebView].
     *
     * @param context Context which should be used to start the [AppActivity].
     * @param enableBackToFinish True if the [AppActivity] should be finished or false if the [cloud.pace.sdk.appkit.app.webview.AppWebView] should navigate back on back press.
     * @param callback Via this callback the client app can subscribe to certain app events.
     */
    @JvmOverloads
    fun openPaymentApp(context: Context, enableBackToFinish: Boolean = true, callback: AppCallbackImpl = defaultAppCallback) {
        appManager.openAppActivity(context, URL.payment, theme, enableBackToFinish, callback)
    }

    /**
     * Starts an [AppActivity] and loads Transactions in the [cloud.pace.sdk.appkit.app.webview.AppWebView].
     *
     * @param context Context which should be used to start the [AppActivity].
     * @param enableBackToFinish True if the [AppActivity] should be finished or false if the [cloud.pace.sdk.appkit.app.webview.AppWebView] should navigate back on back press.
     * @param callback Via this callback the client app can subscribe to certain app events.
     */
    @JvmOverloads
    fun openTransactions(context: Context, enableBackToFinish: Boolean = true, callback: AppCallbackImpl = defaultAppCallback) {
        appManager.openAppActivity(context, URL.transactions, theme, enableBackToFinish, callback)
    }

    /**
     * Starts an [AppActivity] and loads Fueling App in the [cloud.pace.sdk.appkit.app.webview.AppWebView].
     *
     * @param context Context which should be used to start the [AppActivity].
     * @param id Needed to get Fueling App Url for specific gas station
     * @param enableBackToFinish True if the [AppActivity] should be finished or false if the [cloud.pace.sdk.appkit.app.webview.AppWebView] should navigate back on back press.
     * @param callback Via this callback the client app can subscribe to certain app events.
     */
    @JvmOverloads
    fun openFuelingApp(context: Context, id: String? = null, enableBackToFinish: Boolean = true, callback: AppCallbackImpl = defaultAppCallback) {
        appManager.openFuelingApp(context, id, theme, enableBackToFinish, callback)
    }

    /**
     * Starts an [AppActivity] and loads the Connected Fueling dashboard in the [cloud.pace.sdk.appkit.app.webview.AppWebView].
     *
     * @param context Context which should be used to start the [AppActivity].
     * @param enableBackToFinish True if the [AppActivity] should be finished or false if the [cloud.pace.sdk.appkit.app.webview.AppWebView] should navigate back on back press.
     * @param callback Via this callback the client app can subscribe to certain app events.
     */
    @JvmOverloads
    fun openDashboard(context: Context, enableBackToFinish: Boolean = true, callback: AppCallbackImpl = defaultAppCallback) {
        appManager.openAppActivity(context, URL.dashboard, theme, enableBackToFinish, callback)
    }

    /**
     * Closes (finishes) the [AppActivity].
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
