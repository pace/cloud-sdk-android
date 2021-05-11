package cloud.pace.sdk.appkit.location

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.switchMap
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.utils.NoLocationFound
import cloud.pace.sdk.appkit.utils.PermissionDenied
import cloud.pace.sdk.utils.Event
import cloud.pace.sdk.utils.LocationProvider
import cloud.pace.sdk.utils.LocationState
import cloud.pace.sdk.utils.SystemManager
import timber.log.Timber

interface AppLocationManager {

    fun start(callback: (Result<Location>) -> Unit)
    fun stop()
}

class AppLocationManagerImpl(
    private val locationProvider: LocationProvider,
    private val systemManager: SystemManager
) : AppLocationManager {

    private var callback: ((Result<Location>) -> Unit)? = null
    private var startTime = 0L
    private val handler = systemManager.getHandler()
    private val locationTimeoutRunnable = Runnable {
        Timber.w("Timeout after $LOCATION_TIMEOUT ms")
        callback?.invoke(Result.failure(NoLocationFound))
    }
    private val locationEvent = locationProvider.location.switchMap {
        MutableLiveData(Event(it))
    }
    private val locationObserver = Observer<Event<Location>> {
        it.getContentIfNotHandled()?.let { location ->
            if (isLocationValid(location, startTime)) {
                callback?.invoke(Result.success(location))
                stop()
            }
        }
    }

    override fun start(callback: (Result<Location>) -> Unit) {
        this.callback = callback
        startTime = systemManager.getCurrentTimeMillis()
        handler.postDelayed(locationTimeoutRunnable, LOCATION_TIMEOUT)

        when (locationProvider.getLocationState()) {
            LocationState.PERMISSION_DENIED -> {
                callback(Result.failure(PermissionDenied))
                stop()
            }
            LocationState.NO_LOCATION_FOUND -> {
                callback(Result.failure(NoLocationFound))
                stop()
            }
            else -> {
                locationEvent.observeForever(locationObserver)
                locationProvider.requestLocationUpdates()
            }
        }
    }

    private fun isLocationValid(location: Location?, startTime: Long): Boolean {
        if (location == null) {
            Timber.w("Discard null location")
            return false
        }

        // Discard inaccurate locations
        val minAccuracy = getNeededAccuracy(startTime)
        if (location.accuracy > minAccuracy) {
            Timber.w("Discard inaccurate location: location.accuracy (${location.accuracy} m) > minAccuracy ($minAccuracy m)")
            return false
        }

        // Discard old locations
        if (systemManager.getCurrentTimeMillis() - location.time >= MAX_LOCATION_AGE) {
            Timber.w("Discard old location: location.time (${location.time} ms) >= MAX_LOCATION_AGE ($MAX_LOCATION_AGE ms)")
            return false
        }

        Timber.d("App location found: lat = ${location.latitude} lon = ${location.longitude}")
        return true
    }

    private fun getNeededAccuracy(startTime: Long): Int {
        val currentTime = systemManager.getCurrentTimeMillis()
        val requestedTime = currentTime - startTime
        val segmentTime = LOCATION_TIMEOUT / LOCATION_SEGMENTS

        return when {
            requestedTime <= segmentTime -> BEST_ACCURACY
            requestedTime <= segmentTime * 2 -> MEDIUM_ACCURACY
            else -> PACECloudSDK.configuration.locationAccuracy ?: LOW_ACCURACY
        }
    }

    override fun stop() {
        handler.removeCallbacks(locationTimeoutRunnable)
        locationEvent.removeObserver(locationObserver)
        locationProvider.removeLocationUpdates()
    }

    companion object {
        private const val LOCATION_TIMEOUT = 30 * 1000L // 30sec
        private const val LOCATION_SEGMENTS = 5
        private const val MAX_LOCATION_AGE = 60 * 1000 // 1min

        private const val BEST_ACCURACY = 20 // m
        private const val MEDIUM_ACCURACY = 50 // m
        private const val LOW_ACCURACY = 250 // m
    }
}
