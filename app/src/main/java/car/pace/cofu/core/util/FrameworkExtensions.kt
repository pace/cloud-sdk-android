package car.pace.cofu.core.util

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.location.LocationManagerCompat
import java.io.File

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
fun Activity.listenForLocationEnabledChanges(receiver: BroadcastReceiver) {
    registerReceiver(receiver, IntentFilter("android.location.PROVIDERS_CHANGED"))
}

/**
 * Creates a file from a bitmap.
 */
fun Context.writeBitmap(bitmap: Bitmap): File {
    val file = File(filesDir.absolutePath, "share.jpg")
    if (!file.exists()) {
        file.createNewFile()
    }
    file.outputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        out.flush()
    }
    return file
}

/**
 * Checks whether the device is currently connected to a network.
 */
val Context.isOnline: Boolean
    get() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return arrayOf(
            NetworkCapabilities.TRANSPORT_WIFI,
            NetworkCapabilities.TRANSPORT_CELLULAR,
            NetworkCapabilities.TRANSPORT_ETHERNET
        ).any { capabilities.hasTransport(it) }
    }