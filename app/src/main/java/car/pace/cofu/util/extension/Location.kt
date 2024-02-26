package car.pace.cofu.util.extension

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.maps.android.ktx.utils.withSphericalOffset
import kotlin.math.sqrt

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

fun Activity.canShowLocationPermissionDialog(): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
}

fun Location.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun LatLng.toRectangularBounds(radiusInMeters: Double): RectangularBounds {
    val distanceFromCenterToCorner = radiusInMeters * sqrt(2.0)
    val southwestCorner = withSphericalOffset(distanceFromCenterToCorner, 225.0)
    val northeastCorner = withSphericalOffset(distanceFromCenterToCorner, 45.0)

    return RectangularBounds.newInstance(southwestCorner, northeastCorner)
}
