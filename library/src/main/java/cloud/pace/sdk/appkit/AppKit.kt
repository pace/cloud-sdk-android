package cloud.pace.sdk.appkit

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import car.pace.cloudsdk.CloudSDK
import car.pace.cloudsdk.util.DeviceUtils
import cloud.pace.sdk.BuildConfig
import cloud.pace.sdk.appkit.app.AppActivity
import cloud.pace.sdk.appkit.app.drawer.AppDrawer
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.model.Car
import cloud.pace.sdk.appkit.model.Configuration
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.utils.AppKitKoinComponent
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.KoinConfig
import org.koin.core.inject

object AppKit : AppKitKoinComponent {

    private val sharedPreferencesModel: SharedPreferencesModel by inject()
    private val appManager: AppManager by inject()

    internal lateinit var configuration: Configuration
    internal lateinit var userAgent: String

    /**
     * Sets up [AppKit] with the passed [configuration].
     *
     * @param context The context.
     */
    fun setup(context: Context, configuration: Configuration) {
        this.configuration = configuration

        CloudSDK.initialize(
            context, CloudSDK.Configuration(
                idBaseUrl = configuration.environment.idUrl,
                apiBaseUrl = configuration.environment.apiUrl,
                clientId = configuration.clientId ?: "",
                clientVersion = configuration.clientAppVersion,
                clientBuild = configuration.clientAppBuild,
                isDarkTheme = configuration.isDarkTheme
            )
        )

        KoinConfig.setupAppKit(context)
        setUserAgent(configuration)
        sharedPreferencesModel.deleteAllAppStates()
    }

    fun setThemeSetting(isDarkTheme: Boolean) {
        if (::configuration.isInitialized) {
            configuration.isDarkTheme = isDarkTheme
            setUserAgent(configuration)
        }
    }

    fun setUserAgentExtensions(extensions: List<String>) {
        if (::configuration.isInitialized) {
            configuration.extensions = extensions
            setUserAgent(configuration)
        }
    }

    fun setLocationAccuracy(locationAccuracy: Int?) {
        if (::configuration.isInitialized) {
            configuration.locationAccuracy = locationAccuracy
        }
    }

    internal fun setAccessToken(accessToken: String?) {
        if (::configuration.isInitialized) {
            configuration.accessToken = accessToken
            setUserAgent(configuration)
        }
    }

    internal fun resetAccessToken() {
        if (::configuration.isInitialized) {
            configuration.accessToken = null
        }
    }

    private fun setUserAgent(config: Configuration) {
        userAgent = listOf(
            "${config.clientAppName}/${config.clientAppVersion}_${config.clientAppBuild}",
            "(${DeviceUtils.getDeviceName()} Android/${DeviceUtils.getAndroidVersion()})",
            "PWA-SDK/${BuildConfig.VERSION_NAME}",
            if (config.clientId != null) "(clientid:${config.clientId};)" else "",
            if (config.isDarkTheme) "PWASDK-Theme/Dark" else "PWASDK-Theme/Light",
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
     * @param autoClose True if the [AppActivity] should be closed automatically when new apps are opened or no apps come back from the API, false otherwise.
     * @param callback Via this callback the client app can subscribe to certain app events.
     */
    @JvmOverloads
    fun openAppActivity(context: Context, url: String, enableBackToFinish: Boolean = false, autoClose: Boolean = true, callback: AppCallbackImpl? = null) {
        appManager.openAppActivity(context, url, enableBackToFinish, autoClose, callback)
    }

    /**
     * Adds an [AppDrawer] to the parent view [buttonContainer] for each app in [apps] list.
     * Clicking on the [AppDrawer] opens the [AppActivity] and shows the App.
     *
     * @param context Context which should be used to start the [AppActivity].
     * @param isDarkBackground True, if the background of the [AppDrawer] should be dark, false otherwise.
     * @param bottomMargin The margin with which the [AppDrawer]s should be drawn to the bottom edge.
     * @param autoClose True if the [AppActivity] should be closed automatically when new apps are opened or no apps come back from the API, false otherwise.
     * @param callback Via this callback the client app can subscribe to certain app events.
     */
    @JvmOverloads
    fun openApps(
        context: Context,
        apps: List<App>,
        isDarkBackground: Boolean,
        buttonContainer: ConstraintLayout,
        bottomMargin: Float = 16f,
        autoClose: Boolean = true,
        callback: AppCallbackImpl? = null
    ) {
        appManager.openApps(context, apps, isDarkBackground, buttonContainer, bottomMargin, autoClose, callback)
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
