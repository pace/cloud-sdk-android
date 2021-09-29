package cloud.pace.sdk.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import cloud.pace.sdk.PACECloudSDK
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber

interface LocationProvider {

    val locationState: LiveData<LocationState>
    val location: LiveData<Location>

    fun requestLocationUpdates()
    fun removeLocationUpdates()
    suspend fun firstValidLocation(): Completion<Location>
    suspend fun currentLocation(validate: Boolean): Completion<Location?>
    suspend fun lastKnownLocation(validate: Boolean): Completion<Location?>
}

class LocationProviderImpl(
    private val context: Context,
    private val systemManager: SystemManager
) : LocationProvider {

    private val locationProviderReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            requestLocationUpdates()
        }
    }
    private val fusedLocationClient by lazy { systemManager.getFusedLocationProviderClient() }
    private val locationManager by lazy { systemManager.getLocationManager() }
    private val locationRequest by lazy {
        LocationRequest.create().apply {
            interval = LOCATION_REQUEST_INTERVAL
            fastestInterval = LOCATION_REQUEST_FASTEST_INTERVAL
            priority = LOCATION_REQUEST_PRIORITY
            smallestDisplacement = LOCATION_REQUEST_SMALLEST_DISPLACEMENT
        }
    }
    private val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                val lastLocation = locationResult?.lastLocation ?: return
                location.postValue(lastLocation)
            }
        }
    }
    private val locationListener by lazy {
        LocationListener {
            location.postValue(it)
        }
    }
    private val poiKitLocationState = MutableLiveData<LocationState>()

    // Creates a new LiveData that does not emit a value until the source LiveData value has been changed.
    override val locationState = poiKitLocationState.distinctUntilChanged()
    override val location = MutableLiveData<Location>()

    override fun requestLocationUpdates() {
        context.registerReceiver(locationProviderReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))

        if (systemManager.isLocationPermissionGranted()) {
            try {
                val provider = checkLocationState()

                if (systemManager.isGooglePlayServicesAvailable()) {
                    // Use Fused Location Provider API
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                } else if (provider != null) {
                    // Use LocationManager as fallback
                    locationManager?.requestLocationUpdates(provider, LOCATION_REQUEST_INTERVAL, LOCATION_REQUEST_SMALLEST_DISPLACEMENT, locationListener, Looper.getMainLooper())
                }
            } catch (e: SecurityException) {
                Timber.w(PermissionDenied)
                poiKitLocationState.postValue(LocationState.PERMISSION_DENIED)
            }
        } else {
            Timber.w(PermissionDenied)
            poiKitLocationState.postValue(LocationState.PERMISSION_DENIED)
        }
    }

    override fun removeLocationUpdates() {
        try {
            context.unregisterReceiver(locationProviderReceiver)
        } catch (e: IllegalArgumentException) {
        }

        if (systemManager.isGooglePlayServicesAvailable()) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } else {
            locationManager?.removeUpdates(locationListener)
        }
    }

    /**
     * Requests [location updates][com.google.android.gms.location.FusedLocationProviderClient.requestLocationUpdates] from Fused Location Provider API
     * or from [LocationManager][LocationManager.requestLocationUpdates] as fallback and returns the first valid [Location] or a [Throwable].
     *
     * @return The first valid [Location] or a [Throwable] in case of error.
     *
     * @see getLocationIfValid
     */
    override suspend fun firstValidLocation(): Completion<Location> {
        val location = try {
            suspendCoroutineWithTimeout<Location>(LOCATION_TIMEOUT) { continuation ->
                if (systemManager.isLocationPermissionGranted()) {
                    try {
                        val startTime = systemManager.getCurrentTimeMillis()
                        val provider = checkLocationState()

                        when {
                            systemManager.isGooglePlayServicesAvailable() -> {
                                // Use Fused Location Provider API
                                val client = systemManager.getFusedLocationProviderClient()
                                val callback = object : LocationCallback() {
                                    override fun onLocationResult(locationResult: LocationResult?) {
                                        getLocationIfValid(locationResult?.lastLocation, null, startTime)?.let {
                                            client.removeLocationUpdates(this)
                                            continuation.resumeIfActive(it)
                                        }
                                    }
                                }
                                client.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper()).addOnFailureListener {
                                    Timber.e(it, "Could no request location updates with Fused Location Provider API")
                                    poiKitLocationState.postValue(LocationState.NO_LOCATION_FOUND)
                                    continuation.resumeWithExceptionIfActive(it)
                                }
                                continuation.invokeOnCancellation {
                                    client.removeLocationUpdates(callback)
                                }
                            }
                            provider != null -> {
                                // Use LocationManager as fallback
                                val listener = object : LocationListener {
                                    override fun onLocationChanged(location: Location) {
                                        getLocationIfValid(location, null, startTime)?.let {
                                            locationManager?.removeUpdates(this)
                                            continuation.resumeIfActive(it)
                                        }
                                    }

                                    override fun onProviderDisabled(provider: String) {
                                        continuation.resumeWithExceptionIfActive(NoLocationFound)
                                    }

                                    override fun onProviderEnabled(provider: String) {
                                        continuation.resumeWithExceptionIfActive(NoLocationFound)
                                    }

                                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                                        continuation.resumeWithExceptionIfActive(NoLocationFound)
                                    }
                                }
                                locationManager?.requestLocationUpdates(provider, LOCATION_REQUEST_INTERVAL, LOCATION_REQUEST_SMALLEST_DISPLACEMENT, listener, Looper.getMainLooper())
                                continuation.invokeOnCancellation {
                                    locationManager?.removeUpdates(listener)
                                }
                            }
                            else -> {
                                Timber.w(NoLocationFound)
                                poiKitLocationState.postValue(LocationState.NO_LOCATION_FOUND)
                                continuation.resumeWithExceptionIfActive(NoLocationFound)
                            }
                        }
                    } catch (e: SecurityException) {
                        Timber.w(PermissionDenied)
                        poiKitLocationState.postValue(LocationState.PERMISSION_DENIED)
                        continuation.resumeWithExceptionIfActive(PermissionDenied)
                    }
                } else {
                    Timber.w(PermissionDenied)
                    poiKitLocationState.postValue(LocationState.PERMISSION_DENIED)
                    continuation.resumeWithExceptionIfActive(PermissionDenied)
                }
            }
        } catch (e: Exception) {
            return Failure(e)
        }

        return Success(location)
    }

    /**
     * Returns the [current location][com.google.android.gms.location.FusedLocationProviderClient.getCurrentLocation] from Fused Location Provider API
     * or from LocationManager as fallback ([getCurrentLocation][LocationManager.getCurrentLocation] or [requestSingleUpdate][LocationManager.requestSingleUpdate] depending on API level)
     * If [validate] is set to true, it will only return the location if it is valid and `null` otherwise.
     * In case of error it returns a [Throwable].
     *
     * @return The [Location] or `null` depending on the validity or a [Throwable] in case of error.
     *
     * @see getLocationIfValid
     */
    override suspend fun currentLocation(validate: Boolean): Completion<Location?> {
        val location = try {
            suspendCancellableCoroutine<Location?> { continuation ->
                if (systemManager.isLocationPermissionGranted()) {
                    try {
                        val startTime = systemManager.getCurrentTimeMillis()
                        val provider = checkLocationState()

                        when {
                            systemManager.isGooglePlayServicesAvailable() -> {
                                // Use Fused Location Provider API
                                systemManager.getFusedLocationProviderClient().getCurrentLocation(LOCATION_REQUEST_PRIORITY, null)
                                    .addOnSuccessListener {
                                        continuation.resumeIfActive(if (validate) getLocationIfValid(it, LOW_ACCURACY, startTime) else it)
                                    }
                                    .addOnFailureListener {
                                        Timber.e(it, "Could no request location updates with Fused Location Provider API")
                                        poiKitLocationState.postValue(LocationState.NO_LOCATION_FOUND)
                                        continuation.resumeWithExceptionIfActive(it)
                                    }
                            }
                            provider != null -> {
                                // Use LocationManager as fallback
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    locationManager?.getCurrentLocation(provider, null, ContextCompat.getMainExecutor(context)) {
                                        if (it != null) {
                                            continuation.resumeIfActive(if (validate) getLocationIfValid(it, LOW_ACCURACY, startTime) else it)
                                        } else {
                                            Timber.w("No current location available with LocationManager")
                                            poiKitLocationState.postValue(LocationState.NO_LOCATION_FOUND)
                                            continuation.resumeWithExceptionIfActive(NoLocationFound)
                                        }
                                    }
                                } else {
                                    val listener = object : LocationListener {
                                        override fun onLocationChanged(location: Location) {
                                            locationManager?.removeUpdates(this)
                                            continuation.resumeIfActive(if (validate) getLocationIfValid(location, LOW_ACCURACY, startTime) else location)
                                        }

                                        override fun onProviderDisabled(provider: String) {
                                            continuation.resumeWithExceptionIfActive(NoLocationFound)
                                        }

                                        override fun onProviderEnabled(provider: String) {
                                            continuation.resumeWithExceptionIfActive(NoLocationFound)
                                        }

                                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                                            continuation.resumeWithExceptionIfActive(NoLocationFound)
                                        }
                                    }
                                    locationManager?.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                                    continuation.invokeOnCancellation {
                                        locationManager?.removeUpdates(listener)
                                    }
                                }
                            }
                            else -> {
                                Timber.w(NoLocationFound)
                                poiKitLocationState.postValue(LocationState.NO_LOCATION_FOUND)
                                continuation.resumeWithExceptionIfActive(NoLocationFound)
                            }
                        }
                    } catch (e: SecurityException) {
                        Timber.w(PermissionDenied)
                        poiKitLocationState.postValue(LocationState.PERMISSION_DENIED)
                        continuation.resumeWithExceptionIfActive(PermissionDenied)
                    }
                } else {
                    Timber.w(PermissionDenied)
                    poiKitLocationState.postValue(LocationState.PERMISSION_DENIED)
                    continuation.resumeWithExceptionIfActive(PermissionDenied)
                }
            }
        } catch (e: Exception) {
            return Failure(e)
        }

        return Success(location)
    }

    /**
     * Returns the [last location][com.google.android.gms.location.FusedLocationProviderClient.getLastLocation] from Fused Location Provider API
     * or the [last known location][LocationManager.getLastKnownLocation] from LocationManager as fallback.
     * If [validate] is set to true, it will only return the location if it is valid and `null` otherwise.
     * In case of error it returns a [Throwable].
     *
     * @return The [Location] or `null` depending on the validity or a [Throwable] in case of error.
     *
     * @see getLocationIfValid
     */
    override suspend fun lastKnownLocation(validate: Boolean): Completion<Location?> {
        val location = try {
            suspendCancellableCoroutine<Location?> { continuation ->
                if (systemManager.isLocationPermissionGranted()) {
                    try {
                        val startTime = systemManager.getCurrentTimeMillis()
                        val provider = checkLocationState()

                        when {
                            systemManager.isGooglePlayServicesAvailable() -> {
                                // Use Fused Location Provider API
                                systemManager.getFusedLocationProviderClient().lastLocation
                                    .addOnSuccessListener {
                                        continuation.resumeIfActive(if (validate) getLocationIfValid(it, LOW_ACCURACY, startTime) else it)
                                    }
                                    .addOnFailureListener {
                                        Timber.e(it, "Could no request location updates with Fused Location Provider API")
                                        poiKitLocationState.postValue(LocationState.NO_LOCATION_FOUND)
                                        continuation.resumeWithExceptionIfActive(it)
                                    }
                            }
                            provider != null -> {
                                // Use LocationManager as fallback
                                val location = locationManager?.getLastKnownLocation(provider)
                                if (location != null) {
                                    continuation.resumeIfActive(if (validate) getLocationIfValid(location, LOW_ACCURACY, startTime) else location)
                                } else {
                                    Timber.w("No last known location available with LocationManager")
                                    poiKitLocationState.postValue(LocationState.NO_LOCATION_FOUND)
                                    continuation.resumeWithExceptionIfActive(NoLocationFound)
                                }
                            }
                            else -> {
                                Timber.w(NoLocationFound)
                                poiKitLocationState.postValue(LocationState.NO_LOCATION_FOUND)
                                continuation.resumeWithExceptionIfActive(NoLocationFound)
                            }
                        }
                    } catch (e: SecurityException) {
                        Timber.w(PermissionDenied)
                        poiKitLocationState.postValue(LocationState.PERMISSION_DENIED)
                        continuation.resumeWithExceptionIfActive(PermissionDenied)
                    }
                } else {
                    Timber.w(PermissionDenied)
                    poiKitLocationState.postValue(LocationState.PERMISSION_DENIED)
                    continuation.resumeWithExceptionIfActive(PermissionDenied)
                }
            }
        } catch (e: Exception) {
            return Failure(e)
        }

        return Success(location)
    }

    private fun checkLocationState(): String? {
        return when {
            locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true -> {
                poiKitLocationState.postValue(LocationState.LOCATION_HIGH_ACCURACY)
                LocationManager.GPS_PROVIDER
            }
            locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true -> {
                poiKitLocationState.postValue(LocationState.LOCATION_LOW_ACCURACY)
                LocationManager.NETWORK_PROVIDER
            }
            else -> {
                Timber.w(NoLocationFound)
                poiKitLocationState.postValue(LocationState.NO_LOCATION_FOUND)
                null
            }
        }
    }

    private fun getLocationIfValid(location: Location?, minAccuracy: Int?, startTime: Long): Location? {
        if (location == null) {
            Timber.w("Discard null location")
            return null
        }

        // Discard inaccurate locations
        val accuracy = minAccuracy ?: getNeededAccuracy(startTime)
        if (location.accuracy > accuracy) {
            Timber.w("Discard inaccurate location: location.accuracy (${location.accuracy} m) > minAccuracy ($accuracy m)")
            return null
        }

        // Discard old locations
        if (systemManager.getCurrentTimeMillis() - location.time >= MAX_LOCATION_AGE) {
            Timber.w("Discard old location: location.time (${location.time} ms) >= MAX_LOCATION_AGE (${MAX_LOCATION_AGE} ms)")
            return null
        }

        Timber.d("Location found: lat = ${location.latitude} lon = ${location.longitude}")
        return location
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

    companion object {
        private const val LOCATION_REQUEST_INTERVAL = 500L // In ms
        private const val LOCATION_REQUEST_FASTEST_INTERVAL = 200L // In ms
        private const val LOCATION_REQUEST_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY
        private const val LOCATION_REQUEST_SMALLEST_DISPLACEMENT = 0f // In m

        private const val LOCATION_TIMEOUT = 30 * 1000L // 30 sec
        private const val LOCATION_SEGMENTS = 5
        private const val MAX_LOCATION_AGE = 30 * 1000 // 30 sec

        private const val BEST_ACCURACY = 20 // m
        private const val MEDIUM_ACCURACY = 50 // m
        private const val LOW_ACCURACY = 250 // m
    }
}
