package car.pace.cofu.util.extension

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat

val locationPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

val Context.isLocationPermissionGranted: Boolean
    get() = locationPermissions.any(::isPermissionGranted)

/**
 * Checks whether the location is enabled or not.
 */
val Context.isLocationEnabled: Boolean
    get() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(lm)
    }

/**
 * Registers a receiver that will receive an update if the user enables or disables GPS
 * remember to call [Activity.unregisterReceiver].
 */
fun Context.listenForLocationEnabledChanges(receiver: BroadcastReceiver) {
    registerReceiver(receiver, IntentFilter("android.location.PROVIDERS_CHANGED"))
}

fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
