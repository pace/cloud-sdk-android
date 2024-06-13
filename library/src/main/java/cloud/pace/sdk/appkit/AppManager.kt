package cloud.pace.sdk.appkit

import android.content.Context
import android.content.Intent
import android.location.Location
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.app.AppActivity
import cloud.pace.sdk.appkit.app.api.AppRepository
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.model.Car
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl.Companion.getDisableTimePreferenceKey
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.DefaultDispatcherProvider
import cloud.pace.sdk.utils.DispatcherProvider
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.InvalidSpeed
import cloud.pace.sdk.utils.LocationProvider
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.Theme
import cloud.pace.sdk.utils.URL.fueling
import cloud.pace.sdk.utils.equalsTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL
import java.util.Date

internal class AppManager(private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()) : CloudSDKKoinComponent {

    private val locationProvider: LocationProvider by inject()
    private val appRepository: AppRepository by inject()
    private val sharedPreferencesModel: SharedPreferencesModel by inject()
    private val appModel: AppModel by inject()

    private val coroutineScope = CoroutineScope(dispatchers.default())
    private var lastApps = emptyList<String>()

    internal fun requestLocalApps(completion: (Completion<List<App>>) -> Unit) {
        coroutineScope.launch {
            val apps = requestLocalApps()
            completion(apps)
        }
    }

    internal suspend fun requestLocalApps(location: Location? = null): Completion<List<App>> {
        val userLocation = location ?: run {
            when (val firstValidLocation = locationProvider.firstValidLocation()) {
                is Success -> firstValidLocation.result
                is Failure -> return Failure(firstValidLocation.throwable)
            }
        }

        Timber.i("Check local available apps at $userLocation")

        when (val apps = appRepository.getLocationBasedApps(userLocation.latitude, userLocation.longitude)) {
            is Success -> {
                val result = apps.result
                Timber.d("Received ${result.size} apps: ${result.map { it.url }}")

                val notDisabled = filterNotDisabledApps(result)
                val notDisabledUrls = notDisabled.map(App::url)

                if (notDisabledUrls.isNotEmpty()) {
                    /*
                    We're still in the range of the currently open app,
                    therefore we don't have to do anything here.
                     */
                    if (notDisabledUrls.equalsTo(lastApps)) {
                        return Success(notDisabled)
                    }

                    /*
                    In cases where there are new apps, including the old one, which
                    happens in intersections between two stations, and the speed
                    is above the set threshold, we will ignore the new app
                    and keep showing the old one.
                     */
                    if (lastApps.isNotEmpty() && notDisabledUrls.containsAll(lastApps) && !isSpeedValid(userLocation)) {
                        return Success(notDisabled.filter { lastApps.contains(it.url) })
                    }

                    /*
                    In case there are apps, but the location speed is
                    above the given threshold, we will also ignore.

                    In case old apps were set, we will remove them.

                    In both cases an `InvalidSpeed` failure will be passed
                    to the completion callback.
                     */
                    if (!isSpeedValid(userLocation)) {
                        if (lastApps.isNotEmpty()) {
                            lastApps = emptyList()
                        }

                        return Failure(InvalidSpeed)
                    }
                }

                lastApps = notDisabledUrls

                return Success(notDisabled)
            }

            is Failure -> return Failure(apps.throwable)
        }
    }

    private fun isSpeedValid(location: Location): Boolean {
        val metersPerSecond = PACECloudSDK.configuration.speedThresholdInKmPerHour / 3.6
        val isSpeedValid = location.speed < metersPerSecond

        return isSpeedValid && location.speedAccuracyMetersPerSecond < SPEED_ACCURACY_THRESHOLD
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

    internal fun fetchAppsByUrl(url: String, references: List<String>, completion: (Completion<List<App>>) -> Unit) {
        coroutineScope.launch {
            val result = appRepository.getAppsByUrl(url, references)
            withContext(dispatchers.main()) {
                completion(result)
            }
        }
    }

    internal fun fetchUrlByAppId(appId: String, completion: (Completion<String?>) -> Unit) {
        coroutineScope.launch {
            val result = appRepository.getUrlByAppId(appId)
            withContext(dispatchers.main()) {
                completion(result)
            }
        }
    }

    internal fun openAppActivity(context: Context, url: String, theme: Theme, enableBackToFinish: Boolean = false, callback: AppCallbackImpl) {
        callback.onOpen(null)
        startAppActivity(context, url, theme, enableBackToFinish, callback)
    }

    internal fun openAppActivity(context: Context, app: App, theme: Theme, enableBackToFinish: Boolean = false, callback: AppCallbackImpl) {
        callback.onOpen(app)
        startAppActivity(context, app.url, theme, enableBackToFinish, callback)
    }

    internal fun openFuelingApp(context: Context, id: String? = null, theme: Theme, enableBackToFinish: Boolean = true, callback: AppCallbackImpl) {
        if (id == null) {
            openAppActivity(context, fueling, theme, enableBackToFinish, callback)
        } else {
            appRepository.getFuelingUrl(id) {
                openAppActivity(context, it, theme, enableBackToFinish, callback)
            }
        }
    }

    private fun startAppActivity(context: Context, url: String, theme: Theme, enableBackToFinish: Boolean = false, callback: AppCallbackImpl) {
        appModel.callback = callback

        val intent = Intent(context, AppActivity::class.java)
        intent.putExtra(AppActivity.BACK_TO_FINISH, enableBackToFinish)
        intent.putExtra(AppActivity.APP_URL, url)
        intent.putExtra(AppActivity.IS_DARK_MODE, theme == Theme.DARK)
        context.startActivity(intent)
    }

    internal fun closeAppActivity() = appModel.close()

    internal fun setCarData(car: Car) = sharedPreferencesModel.setCar(car)

    companion object {
        private const val SPEED_ACCURACY_THRESHOLD = 3 // in m/s ~= 10km/h
    }
}
