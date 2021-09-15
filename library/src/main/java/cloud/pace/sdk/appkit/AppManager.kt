package cloud.pace.sdk.appkit

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.app.AppActivity
import cloud.pace.sdk.appkit.app.api.AppRepository
import cloud.pace.sdk.appkit.app.drawer.AppDrawer
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.model.Car
import cloud.pace.sdk.appkit.network.NetworkChangeListener
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl.Companion.getDisableTimePreferenceKey
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.inject
import timber.log.Timber
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*

internal class AppManager(private val dispatchers: DispatcherProvider) : CloudSDKKoinComponent {

    private val locationProvider: LocationProvider by inject()
    private val appRepository: AppRepository by inject()
    private val networkChangeListener: NetworkChangeListener by inject()
    private val sharedPreferencesModel: SharedPreferencesModel by inject()
    private val appEventManager: AppEventManager by inject()
    private val appModel: AppModel by inject()

    private var checkRunning = false
    private var lastApps = emptyList<String>()

    internal fun requestLocalApps(completion: (Completion<List<App>>) -> Unit) {
        CoroutineScope(dispatchers.default()).launch {
            Timber.i("Check local available Apps")

            if (checkRunning) {
                Timber.w("App check already running")
                withContext(dispatchers.main()) { completion(Failure(RunningCheck)) }
            } else {
                checkRunning = true

                when (val location = locationProvider.firstValidLocation()) {
                    is Success -> {
                        getAppsByLocation(location.result, completion)
                    }
                    is Failure -> {
                        withContext(dispatchers.main()) { completion(Failure(location.throwable)) }
                    }
                }

                checkRunning = false
            }
        }
    }

    private fun isSpeedValid(location: Location): Boolean {
        val metersPerSecond = PACECloudSDK.configuration.speedThresholdInKmPerHour / 3.6
        var isSpeedValid = location.speed < metersPerSecond

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isSpeedValid = isSpeedValid && location.speedAccuracyMetersPerSecond < SPEED_ACCURACY_THRESHOLD
        }

        return isSpeedValid
    }

    private suspend fun getAppsByLocation(location: Location, completion: (Completion<List<App>>) -> Unit) {
        when (val apps = appRepository.getLocationBasedApps(location.latitude, location.longitude)) {
            is Success -> {
                val result = apps.result
                Timber.d("Received ${result.size} Apps: ${result.map { it.url }}")

                val notDisabled = filterNotDisabledApps(result)
                val notDisabledUrls = notDisabled.map { it.url }
                val disabledUrls = result.map { it.url }.minus(notDisabledUrls)
                val invalidUrls = lastApps.minus(notDisabledUrls)

                appEventManager.setInvalidApps(disabledUrls)

                if (notDisabledUrls.isNotEmpty()) {
                    /*
                    We're still in the range of the currently open app,
                    therefore we don't have to do anything here.
                     */
                    if (notDisabledUrls.equalsTo(lastApps)) {
                        withContext(dispatchers.main()) { completion(Success(notDisabled)) }
                        return
                    }

                    /*
                    In cases where there are new apps, including the old one, which
                    happens in intersections between two stations, and the speed
                    is above the set threshold, we will ignore the new app
                    and keep showing the old one.
                     */
                    if (lastApps.isNotEmpty() && notDisabledUrls.containsAll(lastApps) && !isSpeedValid(location)) {
                        withContext(dispatchers.main()) { completion(Success(notDisabled.filter { lastApps.contains(it.url) })) }
                        return
                    }

                    /*
                    In case there are apps, but the location speed is
                    above the given threshold, we will also ignore.

                    In case old apps were set, we will remove them.

                    In both cases an `InvalidSpeed` failure will be passed
                    to the completion callback.
                     */
                    if (!isSpeedValid(location)) {
                        if (lastApps.isNotEmpty()) {
                            appEventManager.setInvalidApps(lastApps)
                            lastApps = emptyList()
                        }

                        withContext(dispatchers.main()) { completion(Failure(InvalidSpeed)) }

                        return
                    }
                }

                appEventManager.setInvalidApps(invalidUrls)
                lastApps = notDisabledUrls

                withContext(dispatchers.main()) { completion(Success(notDisabled)) }
            }
            is Failure -> {
                val error = apps.throwable
                Timber.e(error, "Could not receive Apps")

                if (error is IOException) {
                    Timber.w("No network - listen to network changes")
                    networkChangeListener.getNetworkChanges { networkChanged ->
                        if (networkChanged) {
                            requestLocalApps(completion)
                        } else {
                            Timber.e(NetworkError)
                            CoroutineScope(dispatchers.main()).launch { completion(Failure(NetworkError)) }
                        }
                    }
                } else {
                    Timber.e(NetworkError)
                    withContext(dispatchers.main()) { completion(Failure(NetworkError)) }
                }
            }
        }
    }

    private fun filterNotDisabledApps(apps: List<App>): List<App> {
        return apps.filter {
            try {
                val host = URL(it.url).host
                val timestamp = sharedPreferencesModel.getLong(getDisableTimePreferenceKey(host))
                when {
                    timestamp == null -> true
                    Date(timestamp).after(Date()) -> {
                        Timber.d("Don't show app $host, because disable timer has not been reached (timestamp = $timestamp)")
                        false
                    }
                    else -> {
                        Timber.d("Disable timer for app $host has been reached")
                        sharedPreferencesModel.remove(getDisableTimePreferenceKey(host))
                        true
                    }
                }
            } catch (e: MalformedURLException) {
                false
            }
        }
    }

    internal fun requestApps(completion: (Completion<List<App>>) -> Unit) {
        CoroutineScope(dispatchers.default()).launch {
            val result = appRepository.getAllApps()
            withContext(dispatchers.main()) {
                completion(result)
            }
        }
    }

    internal fun fetchAppsByUrl(url: String, references: List<String>, completion: (Completion<List<App>>) -> Unit) {
        CoroutineScope(dispatchers.default()).launch {
            val result = appRepository.getAppsByUrl(url, references)
            withContext(dispatchers.main()) {
                completion(result)
            }
        }
    }

    internal fun fetchUrlByAppId(appId: String, completion: (Completion<String?>) -> Unit) {
        CoroutineScope(dispatchers.default()).launch {
            val result = appRepository.getUrlByAppId(appId)
            withContext(dispatchers.main()) {
                completion(result)
            }
        }
    }

    internal fun openAppActivity(context: Context, url: String, enableBackToFinish: Boolean = false, callback: AppCallbackImpl) {
        callback.onOpen(null)
        startAppActivity(context, url, enableBackToFinish, callback)
    }

    internal fun openAppActivity(context: Context, app: App, enableBackToFinish: Boolean = false, callback: AppCallbackImpl) {
        callback.onOpen(app)
        startAppActivity(context, app.url, enableBackToFinish, callback)
    }

    private fun startAppActivity(context: Context, url: String, enableBackToFinish: Boolean = false, callback: AppCallbackImpl) {
        appModel.callback = callback

        val intent = Intent(context, AppActivity::class.java)
        intent.putExtra(AppActivity.BACK_TO_FINISH, enableBackToFinish)
        intent.putExtra(AppActivity.APP_URL, url)
        context.startActivity(intent)
    }

    internal fun openApps(context: Context, apps: List<App>, buttonContainer: ConstraintLayout, theme: Theme, bottomMargin: Float, callback: AppCallbackImpl) {
        closeApps(buttonContainer)

        var topAppDrawerId: Int? = null
        apps.forEach { app ->
            val appDrawer = AppDrawer(context, null)
            appDrawer.visibility = View.GONE
            appDrawer.id = View.generateViewId()
            appDrawer.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)

            appDrawer.setApp(app, theme == Theme.DARK) {
                openAppActivity(context, app, callback = callback)
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
        Timber.i("Close all AppDrawers")
        // asReversed(), to make sure that all AppDrawers will be removed, because iterating and removing is done simultaneously
        buttonContainer.children.toList().asReversed().forEach {
            if (it is AppDrawer) {
                Timber.d("Child is AppDrawer --> Remove view with ID ${it.id}")
                buttonContainer.removeView(it)
            }
        }

        appModel.close()
    }

    internal fun closeAppActivity() = appModel.close()

    internal fun setCarData(car: Car) = sharedPreferencesModel.setCar(car)

    companion object {
        private const val SPEED_ACCURACY_THRESHOLD = 3 // in m/s ~= 10km/h
    }
}
