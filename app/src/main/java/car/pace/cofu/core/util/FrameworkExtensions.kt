package car.pace.cofu.core.util

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat

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
