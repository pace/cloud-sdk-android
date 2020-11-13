package cloud.pace.sdk.utils

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

interface SystemManager {

    fun isLocationPermissionGranted(): Boolean
    fun isGooglePlayServicesAvailable(): Boolean
    fun getFusedLocationProviderClient(): FusedLocationProviderClient
    fun getLocationManager(): LocationManager?
    fun getConnectivityManager(): ConnectivityManager?
    fun getHandler(): Handler
    fun getCurrentTimeMillis(): Long
}

class SystemManagerImpl(private val context: Context) : SystemManager {

    override fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun isGooglePlayServicesAvailable(): Boolean {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }

    override fun getFusedLocationProviderClient(): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    override fun getLocationManager(): LocationManager? {
        return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    }

    override fun getConnectivityManager(): ConnectivityManager? {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    }

    override fun getHandler(): Handler {
        return Handler(Looper.getMainLooper())
    }

    override fun getCurrentTimeMillis(): Long {
        return System.currentTimeMillis()
    }
}
