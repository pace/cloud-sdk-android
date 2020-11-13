package cloud.pace.sdk.appkit.location

import android.location.Location
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.appkit.utils.NoLocationFound
import cloud.pace.sdk.appkit.utils.PermissionDenied
import cloud.pace.sdk.utils.LocationProvider
import cloud.pace.sdk.utils.LocationState
import cloud.pace.sdk.utils.Log
import cloud.pace.sdk.utils.SystemManager

interface AppLocationManager {

    fun getLocation(callback: (Result<Location>) -> Unit)
}

class AppLocationManagerImpl(
    private val locationProvider: LocationProvider,
    private val systemManager: SystemManager
) : AppLocationManager {

    override fun getLocation(callback: (Result<Location>) -> Unit) {
        val startTime = systemManager.getCurrentTimeMillis()
        val handler = systemManager.getHandler()
        val locationTimeoutRunnable = Runnable {
            Log.w("AppLocationManager timeout after $LOCATION_TIMEOUT ms")
            callback(Result.failure(NoLocationFound))
        }
        handler.postDelayed(locationTimeoutRunnable, LOCATION_TIMEOUT)

        locationProvider.location.observeForever {
            if (isLocationValid(it, startTime)) {
                locationProvider.removeLocationUpdates()
                handler.removeCallbacks(locationTimeoutRunnable)
                callback(Result.success(it))
            }
        }

        locationProvider.locationState.observeForever {
            if (it == LocationState.PERMISSION_DENIED) {
                locationProvider.removeLocationUpdates()
                handler.removeCallbacks(locationTimeoutRunnable)
                callback(Result.failure(PermissionDenied))
            } else if (it == LocationState.NO_LOCATION_FOUND) {
                locationProvider.removeLocationUpdates()
                handler.removeCallbacks(locationTimeoutRunnable)
                callback(Result.failure(NoLocationFound))
            }
        }

        locationProvider.requestLocationUpdates()
    }

    private fun isLocationValid(location: Location?, startTime: Long): Boolean {
        if (location == null) {
            Log.w("Discard null location")
            return false
        }

        // Discard inaccurate locations
        val minAccuracy = getNeededAccuracy(startTime)
        if (location.accuracy > minAccuracy) {
            Log.w("Discard inaccurate location: location.accuracy (${location.accuracy} m) > minAccuracy ($minAccuracy m)")
            return false
        }

        // Discard old locations
        if (systemManager.getCurrentTimeMillis() - location.time >= MAX_LOCATION_AGE) {
            Log.w("Discard old location: location.time (${location.time} ms) >= MAX_LOCATION_AGE ($MAX_LOCATION_AGE ms)")
            return false
        }

        Log.d("App location found: lat = ${location.latitude} lon = ${location.longitude}")
        return true
    }

    private fun getNeededAccuracy(startTime: Long): Int {
        val currentTime = systemManager.getCurrentTimeMillis()
        val requestedTime = currentTime - startTime
        val segmentTime = LOCATION_TIMEOUT / LOCATION_SEGMENTS

        return when {
            requestedTime <= segmentTime -> BEST_ACCURACY
            requestedTime <= segmentTime * 2 -> MEDIUM_ACCURACY
            else -> AppKit.configuration.locationAccuracy ?: LOW_ACCURACY
        }
    }

    companion object {
        private const val LOCATION_TIMEOUT = 30 * 1000L // 30sec
        private const val LOCATION_SEGMENTS = 5
        private const val MAX_LOCATION_AGE = 60 * 1000 // 1min

        private const val BEST_ACCURACY = 20 // m
        private const val MEDIUM_ACCURACY = 50 // m
        private const val LOW_ACCURACY = 200 // m
    }
}
