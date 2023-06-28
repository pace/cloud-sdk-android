package cloud.pace.sdk.utils

import android.content.Context
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
import cloud.pace.sdk.utils.LocationProviderImpl.Companion.DEFAULT_LOCATION_REQUEST
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

interface LocationProvider {

    val locationState: LiveData<LocationState>
    val location: LiveData<Location>

    fun requestLocationUpdates(locationRequest: LocationRequest = DEFAULT_LOCATION_REQUEST)
    fun removeLocationUpdates()
    fun locationFlow(locationRequest: LocationRequest = DEFAULT_LOCATION_REQUEST): Flow<Location>
    suspend fun firstValidLocation(timeout: Long = LocationProviderImpl.LOCATION_TIMEOUT): Completion<Location>
    suspend fun currentLocation(validate: Boolean, timeout: Long = LocationProviderImpl.LOCATION_TIMEOUT): Completion<Location?>
    suspend fun lastKnownLocation(validate: Boolean, timeout: Long = LocationProviderImpl.LOCATION_TIMEOUT): Completion<Location?>
}

class LocationProviderImpl(
    private val context: Context,
    private val systemManager: SystemManager
) : LocationProvider {

    private val fusedLocationClient by lazy { systemManager.getFusedLocationProviderClient() }
    private val locationManager by lazy { systemManager.getLocationManager() }
    private val locationCallback by lazy {
        createLocationCallback {
            location.postValue(it)
        }
    }
    private val locationListener by lazy {
        createLocationListener {
            location.postValue(it)
        }
    }
    private val poiKitLocationState = MutableLiveData<LocationState>()

    // Creates a new LiveData that does not emit a value until the source LiveData value has been changed.
    override val locationState = poiKitLocationState.distinctUntilChanged()
    override val location = MutableLiveData<Location>()

    override fun requestLocationUpdates(locationRequest: LocationRequest) {
        if (systemManager.isLocationPermissionGranted()) {
            try {
                val provider = checkLocationState()

                if (systemManager.isGooglePlayServicesAvailable()) {
                    // Use Fused Location Provider API
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                } else {
                    // Use LocationManager as fallback
                    locationManager?.requestLocationUpdates(provider, locationRequest.intervalMillis, locationRequest.minUpdateDistanceMeters, locationListener, Looper.getMainLooper())
                }
            } catch (e: SecurityException) {
                resumeWithPermissionDenied()
            }
        } else {
            resumeWithPermissionDenied()
        }
    }

    override fun removeLocationUpdates() {
        if (systemManager.isGooglePlayServicesAvailable()) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } else {
            locationManager?.removeUpdates(locationListener)
        }
    }

    override fun locationFlow(locationRequest: LocationRequest): Flow<Location> {
        return if (systemManager.isGooglePlayServicesAvailable()) {
            // Use Fused Location Provider API
            fusedLocationProviderUpdates(locationRequest)
        } else {
            // Use LocationManager as fallback
            locationListenerUpdates(locationRequest)
        }
    }

    private fun fusedLocationProviderUpdates(locationRequest: LocationRequest) = callbackFlow {
        val locationCallback = createLocationCallback {
            trySend(it)
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            Timber.w(PermissionDenied)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun locationListenerUpdates(locationRequest: LocationRequest) = callbackFlow {
        val locationListener = createLocationListener {
            trySend(it)
        }

        try {
            val provider = checkLocationState()
            locationManager?.requestLocationUpdates(provider, locationRequest.intervalMillis, locationRequest.minUpdateDistanceMeters, locationListener, Looper.getMainLooper())
        } catch (e: SecurityException) {
            Timber.w(PermissionDenied)
        }

        awaitClose {
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
    override suspend fun firstValidLocation(timeout: Long): Completion<Location> {
        val location = try {
            suspendCoroutineWithTimeout<Location>(timeout) { continuation ->
                if (systemManager.isLocationPermissionGranted()) {
                    try {
                        val startTime = systemManager.getCurrentTimeMillis()
                        val provider = checkLocationState()
                        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500L).build()

                        if (systemManager.isGooglePlayServicesAvailable()) {
                            // Use Fused Location Provider API
                            val client = systemManager.getFusedLocationProviderClient()
                            val callback = object : LocationCallback() {
                                override fun onLocationResult(locationResult: LocationResult) {
                                    getLocationIfValid(locationResult.lastLocation, null, startTime)?.let {
                                        client.removeLocationUpdates(this)
                                        continuation.resumeIfActive(it)
                                    }
                                }
                            }
                            client.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper()).addOnFailureListener {
                                Timber.w(it, "Could no request location updates with Fused Location Provider API")
                                poiKitLocationState.postValue(LocationState.NO_LOCATION_FOUND)
                                continuation.resumeWithExceptionIfActive(it)
                            }
                            continuation.invokeOnCancellation {
                                client.removeLocationUpdates(callback)
                            }
                        } else {
                            // Use LocationManager as fallback
                            val listener = object : LocationListener {
                                override fun onLocationChanged(location: Location) {
                                    getLocationIfValid(location, null, startTime)?.let {
                                        locationManager?.removeUpdates(this)
                                        continuation.resumeIfActive(it)
                                    }
                                }

                                override fun onProviderEnabled(provider: String) {
                                    handleLocationStateChange(provider, true, this, continuation)
                                }

                                override fun onProviderDisabled(provider: String) {
                                    resumeWithNoLocationFound(this, continuation)
                                }

                                @Deprecated("Deprecated in Java")
                                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                                    handleLocationStateChange(provider, provider != null && locationManager?.isProviderEnabled(provider) == true, this, continuation)
                                }
                            }

                            locationManager?.requestLocationUpdates(provider, locationRequest.intervalMillis, locationRequest.minUpdateDistanceMeters, listener, Looper.getMainLooper())
                            continuation.invokeOnCancellation {
                                locationManager?.removeUpdates(listener)
                            }
                        }
                    } catch (e: SecurityException) {
                        resumeWithPermissionDenied(continuation)
                    }
                } else {
                    resumeWithPermissionDenied(continuation)
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
    override suspend fun currentLocation(validate: Boolean, timeout: Long): Completion<Location?> {
        val location = try {
            suspendCoroutineWithTimeout<Location?>(timeout) { continuation ->
                if (systemManager.isLocationPermissionGranted()) {
                    try {
                        val startTime = systemManager.getCurrentTimeMillis()
                        val provider = checkLocationState()

                        if (systemManager.isGooglePlayServicesAvailable()) {
                            // Use Fused Location Provider API
                            systemManager.getFusedLocationProviderClient().getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                                .addOnSuccessListener {
                                    continuation.resumeIfActive(if (validate) getLocationIfValid(it, LOW_ACCURACY, startTime) else it)
                                }
                                .addOnFailureListener {
                                    Timber.w(it, "Could no request location updates with Fused Location Provider API")
                                    poiKitLocationState.postValue(LocationState.NO_LOCATION_FOUND)
                                    continuation.resumeWithExceptionIfActive(it)
                                }
                        } else {
                            // Use LocationManager as fallback
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                locationManager?.getCurrentLocation(provider, null, ContextCompat.getMainExecutor(context)) {
                                    if (it != null) {
                                        continuation.resumeIfActive(if (validate) getLocationIfValid(it, LOW_ACCURACY, startTime) else it)
                                    } else {
                                        Timber.i("No current location available with LocationManager")
                                        resumeWithNoLocationFound(continuation = continuation)
                                    }
                                }
                            } else {
                                val listener = object : LocationListener {
                                    override fun onLocationChanged(location: Location) {
                                        locationManager?.removeUpdates(this)
                                        continuation.resumeIfActive(if (validate) getLocationIfValid(location, LOW_ACCURACY, startTime) else location)
                                    }

                                    override fun onProviderEnabled(provider: String) {
                                        handleLocationStateChange(provider, true, this, continuation)
                                    }

                                    override fun onProviderDisabled(provider: String) {
                                        resumeWithNoLocationFound(this, continuation)
                                    }

                                    @Deprecated("Deprecated in Java")
                                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                                        handleLocationStateChange(provider, provider != null && locationManager?.isProviderEnabled(provider) == true, this, continuation)
                                    }
                                }

                                @Suppress("DEPRECATION")
                                locationManager?.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                                continuation.invokeOnCancellation {
                                    locationManager?.removeUpdates(listener)
                                }
                            }
                        }
                    } catch (e: SecurityException) {
                        resumeWithPermissionDenied(continuation)
                    }
                } else {
                    resumeWithPermissionDenied(continuation)
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
    override suspend fun lastKnownLocation(validate: Boolean, timeout: Long): Completion<Location?> {
        val location = try {
            suspendCoroutineWithTimeout<Location?>(timeout) { continuation ->
                if (systemManager.isLocationPermissionGranted()) {
                    try {
                        val startTime = systemManager.getCurrentTimeMillis()
                        val provider = checkLocationState()

                        if (systemManager.isGooglePlayServicesAvailable()) {
                            // Use Fused Location Provider API
                            systemManager.getFusedLocationProviderClient().lastLocation
                                .addOnSuccessListener {
                                    continuation.resumeIfActive(if (validate) getLocationIfValid(it, LOW_ACCURACY, startTime) else it)
                                }
                                .addOnFailureListener {
                                    Timber.w(it, "Could no request location updates with Fused Location Provider API")
                                    poiKitLocationState.postValue(LocationState.NO_LOCATION_FOUND)
                                    continuation.resumeWithExceptionIfActive(it)
                                }
                        } else {
                            // Use LocationManager as fallback
                            val location = locationManager?.getLastKnownLocation(provider)
                            if (location != null) {
                                continuation.resumeIfActive(if (validate) getLocationIfValid(location, LOW_ACCURACY, startTime) else location)
                            } else {
                                Timber.i("No last known location available with LocationManager")
                                resumeWithNoLocationFound(continuation = continuation)
                            }
                        }
                    } catch (e: SecurityException) {
                        resumeWithPermissionDenied(continuation)
                    }
                } else {
                    resumeWithPermissionDenied(continuation)
                }
            }
        } catch (e: Exception) {
            return Failure(e)
        }

        return Success(location)
    }

    private fun createLocationCallback(onLocationChanged: (Location) -> Unit): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                if (lastLocation != null) {
                    onLocationChanged(lastLocation)
                } else {
                    resumeWithNoLocationFound()
                }
            }
        }
    }

    private fun createLocationListener(onLocationChanged: (Location) -> Unit): LocationListener {
        return object : LocationListener {
            override fun onLocationChanged(newLocation: Location) {
                onLocationChanged(newLocation)
            }

            override fun onProviderEnabled(provider: String) {
                handleLocationStateChange(provider, true)
            }

            override fun onProviderDisabled(provider: String) {
                resumeWithNoLocationFound()
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                handleLocationStateChange(provider, provider != null && locationManager?.isProviderEnabled(provider) == true)
            }
        }
    }

    private fun checkLocationState(): String {
        return if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
            poiKitLocationState.postValue(LocationState.LOCATION_HIGH_ACCURACY)
            LocationManager.GPS_PROVIDER
        } else {
            poiKitLocationState.postValue(LocationState.LOCATION_LOW_ACCURACY)
            LocationManager.NETWORK_PROVIDER
        }
    }

    private fun handleLocationStateChange(provider: String?, isProviderEnabled: Boolean, locationListener: LocationListener? = null, continuation: CancellableContinuation<Location>? = null) {
        if (provider != null && isProviderEnabled) {
            when (provider) {
                LocationManager.GPS_PROVIDER -> poiKitLocationState.postValue(LocationState.LOCATION_HIGH_ACCURACY) // Wait for onLocationChanged
                LocationManager.NETWORK_PROVIDER -> poiKitLocationState.postValue(LocationState.LOCATION_LOW_ACCURACY) // Wait for onLocationChanged
                else -> resumeWithNoLocationFound(locationListener, continuation)
            }
        } else {
            resumeWithNoLocationFound(locationListener, continuation)
        }
    }

    private fun resumeWithNoLocationFound(locationListener: LocationListener? = null, continuation: CancellableContinuation<Location>? = null) {
        Timber.w(NoLocationFound)
        poiKitLocationState.postValue(LocationState.NO_LOCATION_FOUND)
        if (locationListener != null) locationManager?.removeUpdates(locationListener)
        continuation?.resumeWithExceptionIfActive(NoLocationFound)
    }

    private fun resumeWithPermissionDenied(continuation: CancellableContinuation<Location>? = null) {
        Timber.w(PermissionDenied)
        poiKitLocationState.postValue(LocationState.PERMISSION_DENIED)
        continuation?.resumeWithExceptionIfActive(PermissionDenied)
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
            Timber.w("Discard old location: location.time (${location.time} ms) >= MAX_LOCATION_AGE ($MAX_LOCATION_AGE ms)")
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

        private const val DEFAULT_INTERVAL_MILLIS = 2000L // 2 sec
        val DEFAULT_LOCATION_REQUEST = LocationRequest.Builder(DEFAULT_INTERVAL_MILLIS).build()

        const val LOCATION_TIMEOUT = 30 * 1000L // 30 sec
        private const val LOCATION_SEGMENTS = 5
        private const val MAX_LOCATION_AGE = 30 * 1000 // 30 sec

        private const val BEST_ACCURACY = 20 // m
        private const val MEDIUM_ACCURACY = 50 // m
        private const val LOW_ACCURACY = 250 // m
    }
}
