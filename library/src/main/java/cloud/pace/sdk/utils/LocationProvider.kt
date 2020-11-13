package cloud.pace.sdk.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

interface LocationProvider {

    val locationState: LiveData<LocationState>
    val location: LiveData<Location>

    fun requestLocationUpdates()
    fun getLastKnownLocation(completion: (Location?) -> Unit)
    fun removeLocationUpdates()
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
                    locationManager?.requestLocationUpdates(provider, LOCATION_REQUEST_INTERVAL, LOCATION_REQUEST_SMALLEST_DISPLACEMENT, locationListener)
                }
            } catch (e: SecurityException) {
                poiKitLocationState.postValue(LocationState.PERMISSION_DENIED)
            }
        } else {
            poiKitLocationState.postValue(LocationState.PERMISSION_DENIED)
        }
    }

    override fun getLastKnownLocation(completion: (Location?) -> Unit) {
        if (systemManager.isLocationPermissionGranted()) {
            try {
                val provider = checkLocationState()

                if (systemManager.isGooglePlayServicesAvailable()) {
                    // Use Fused Location Provider API
                    fusedLocationClient.getCurrentLocation(LOCATION_REQUEST_PRIORITY, null).addOnCompleteListener {
                        if (it.isSuccessful) {
                            completion(it.result)
                        } else {
                            completion(null)
                        }
                    }
                } else if (provider != null) {
                    // Use LocationManager as fallback
                    completion(locationManager?.getLastKnownLocation(provider))
                }
            } catch (e: SecurityException) {
                poiKitLocationState.postValue(LocationState.PERMISSION_DENIED)
            }
        } else {
            poiKitLocationState.postValue(LocationState.PERMISSION_DENIED)
        }
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
                poiKitLocationState.postValue(LocationState.NO_LOCATION_FOUND)
                null
            }
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

    companion object {
        private const val LOCATION_REQUEST_INTERVAL = 500L // In ms
        private const val LOCATION_REQUEST_FASTEST_INTERVAL = 200L // In ms
        private const val LOCATION_REQUEST_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY
        private const val LOCATION_REQUEST_SMALLEST_DISPLACEMENT = 0f // In m
    }
}
