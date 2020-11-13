package cloud.pace.sdk.appkit.geofences

import com.google.android.gms.location.GeofencingEvent

open class GeofenceCallback {

    var callback: (event: GeofencingEvent) -> Unit = {}

    fun onGeofenceEvent(event: GeofencingEvent) {
        callback(event)
    }
}
