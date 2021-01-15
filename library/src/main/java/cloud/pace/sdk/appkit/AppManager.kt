package cloud.pace.sdk.appkit

import android.content.Context
import android.content.Intent
import android.location.Location
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import cloud.pace.sdk.appkit.app.AppActivity
import cloud.pace.sdk.appkit.app.api.AppRepository
import cloud.pace.sdk.appkit.app.drawer.AppDrawer
import cloud.pace.sdk.appkit.app.webview.AppWebViewModelImpl.Companion.getDisableTimePreferenceKey
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.location.AppLocationManager
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.model.Car
import cloud.pace.sdk.appkit.network.NetworkChangeListener
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.appkit.utils.NetworkError
import cloud.pace.sdk.appkit.utils.RunningCheck
import cloud.pace.sdk.utils.*
import org.koin.core.inject
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*

internal class AppManager : CloudSDKKoinComponent {

    private val context: Context by inject()
    private val appLocationManager: AppLocationManager by inject()
    private val appRepository: AppRepository by inject()
    private val networkChangeListener: NetworkChangeListener by inject()
    private val sharedPreferencesModel: SharedPreferencesModel by inject()
    private val appEventManager: AppEventManager by inject()
    private val appModel: AppModel by inject()

    private var checkRunning = false
    private var lastApps = emptyList<String>()

    internal fun requestLocalApps(completion: (Completion<List<App>>) -> Unit) {
        Log.d("Check local available Apps")

        if (checkRunning) {
            Log.w("App check already running")
            completion(Failure(RunningCheck))
            return
        }
        checkRunning = true

        appLocationManager.start { result ->
            result.onSuccess {
                getAppsByLocation(it, completion)
            }

            result.onFailure {
                completion(Failure(it))
            }

            checkRunning = false
        }
    }

    /**
     * Requests Apps from Cloud API.
     *
     * @param location Current location
     * @param completion Returns a list of [App]s on success or a [Throwable] on failure
     */
    private fun getAppsByLocation(location: Location, completion: (Completion<List<App>>) -> Unit) {
        appRepository.getLocationBasedApps(context, location.latitude, location.longitude) { result ->
            result.onSuccess { apps ->
                Log.d("Received ${apps.size} Apps: ${apps.map { it.url }}")

                val notDisabled = apps
                    .filter {
                        try {
                            val host = URL(it.url).host
                            val timestamp = sharedPreferencesModel.getLong(getDisableTimePreferenceKey(host))
                            when {
                                timestamp == null -> true
                                Date(timestamp).after(Date()) -> {
                                    Log.d("Don't show app $host, because disable timer has not been reached (timestamp = $timestamp)")
                                    false
                                }
                                else -> {
                                    Log.d("Disable timer for app $host has been reached")
                                    sharedPreferencesModel.remove(getDisableTimePreferenceKey(host))
                                    true
                                }
                            }
                        } catch (e: MalformedURLException) {
                            false
                        }
                    }

                val notDisabledUrls = notDisabled.map { it.url }
                val invalidUrls = lastApps.minus(notDisabledUrls)
                appEventManager.setInvalidApps(invalidUrls)
                appModel.close(urls = invalidUrls)

                lastApps = notDisabledUrls

                completion(Success(notDisabled))
            }

            result.onFailure { error ->
                Log.e(error, "Could not receive Apps: ${error.message}")

                if (error is IOException) {
                    Log.d("No network - listen to network changes")
                    networkChangeListener.getNetworkChanges { networkChanged ->
                        if (networkChanged) {
                            requestLocalApps(completion)
                        } else {
                            completion(Failure(NetworkError))
                        }
                    }
                } else {
                    completion(Failure(NetworkError))
                }
            }
        }
    }

    internal fun requestApps(completion: (Completion<List<App>>) -> Unit) {
        appRepository.getAllApps(context) { result ->
            result.onSuccess { completion(Success(it)) }
            result.onFailure { completion(Failure(it)) }
        }
    }

    internal fun fetchAppsByUrl(url: String, references: List<String>, completion: (Completion<List<App>>) -> Unit) {
        appRepository.getAppsByUrl(context, url, references) { result ->
            result.onSuccess { completion(Success(it)) }
            result.onFailure { completion(Failure(it)) }
        }
    }

    internal fun fetchUrlByAppId(appId: String, completion: (Completion<String?>) -> Unit) {
        appRepository.getUrlByAppId(appId) { result ->
            result.onSuccess { completion(Success(it)) }
            result.onFailure { completion(Failure(it)) }
        }
    }

    internal fun isPoiInRange(poiId: String, completion: (Boolean) -> Unit) {
        requestLocalApps {
            when (it) {
                is Success -> completion(it.result.any { app -> app.gasStationId == poiId })
                is Failure -> completion(false)
            }
        }
    }

    internal fun openAppActivity(context: Context, url: String, enableBackToFinish: Boolean = false, autoClose: Boolean, callback: AppCallbackImpl? = null) {
        callback?.onOpen(null)
        startAppActivity(context, url, enableBackToFinish, autoClose, callback)
    }

    internal fun openAppActivity(context: Context, app: App, enableBackToFinish: Boolean = false, autoClose: Boolean, callback: AppCallbackImpl? = null) {
        callback?.onOpen(app)
        startAppActivity(context, app.url, enableBackToFinish, autoClose, callback)
    }

    private fun startAppActivity(context: Context, url: String, enableBackToFinish: Boolean = false, autoClose: Boolean, callback: AppCallbackImpl? = null) {
        appModel.callback = callback

        val intent = Intent(context, AppActivity::class.java)
        intent.putExtra(AppActivity.BACK_TO_FINISH, enableBackToFinish)
        intent.putExtra(AppActivity.APP_URL, url)
        intent.putExtra(AppActivity.AUTO_CLOSE, autoClose)
        context.startActivity(intent)
    }

    internal fun openApps(context: Context, apps: List<App>, buttonContainer: ConstraintLayout, theme: Theme, bottomMargin: Float, autoClose: Boolean, callback: AppCallbackImpl?) {
        closeApps(buttonContainer)

        var topAppDrawerId: Int? = null
        apps.forEach { app ->
            val appDrawer = AppDrawer(context, null)
            appDrawer.visibility = View.GONE
            appDrawer.id = View.generateViewId()
            appDrawer.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)

            appDrawer.setApp(app, theme == Theme.DARK) {
                openAppActivity(context, app, autoClose = autoClose, callback = callback)
            }
            appDrawer.expand()

            buttonContainer.addView(appDrawer)

            val constraintSet = ConstraintSet()
            constraintSet.clone(buttonContainer)

            if (topAppDrawerId == null) {
                constraintSet.connect(appDrawer.id, ConstraintSet.BOTTOM, topAppDrawerId ?: buttonContainer.id, ConstraintSet.BOTTOM, bottomMargin.dp)
            } else {
                constraintSet.connect(appDrawer.id, ConstraintSet.BOTTOM, topAppDrawerId ?: buttonContainer.id, ConstraintSet.TOP)
            }
            constraintSet.connect(appDrawer.id, ConstraintSet.START, buttonContainer.id, ConstraintSet.START)
            constraintSet.connect(appDrawer.id, ConstraintSet.END, buttonContainer.id, ConstraintSet.END)
            constraintSet.applyTo(buttonContainer)

            topAppDrawerId = appDrawer.id

            appDrawer.show()
        }
    }

    internal fun closeApps(buttonContainer: ConstraintLayout) {
        Log.d("Close all AppDrawers")
        // asReversed(), to make sure that all AppDrawers will be removed, because iterating and removing is done simultaneously
        buttonContainer.children.toList().asReversed().forEach {
            if (it is AppDrawer) {
                Log.d("Child is AppDrawer --> Remove view with ID ${it.id}")
                buttonContainer.removeView(it)
            }
        }

        appModel.close()
    }

    internal fun closeAppActivity() = appModel.close(true)

    internal fun setCarData(car: Car) = sharedPreferencesModel.setCar(car)
}
