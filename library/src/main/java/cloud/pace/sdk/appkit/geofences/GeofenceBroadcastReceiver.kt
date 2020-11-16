package cloud.pace.sdk.appkit.geofences

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cloud.pace.sdk.utils.AppKitKoinComponent
import com.google.android.gms.location.GeofencingEvent
import org.koin.core.inject

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val helper by lazy { BroadcastReceiverHelper() }

    override fun onReceive(context: Context?, intent: Intent?) {
        helper.onReceive(context, intent)
    }
}

class BroadcastReceiverHelper : AppKitKoinComponent {

    private val eventCallback: GeofenceCallback by inject()

    fun onReceive(@Suppress("UNUSED_PARAMETER") context: Context?, intent: Intent?) {
        eventCallback.onGeofenceEvent(GeofencingEvent.fromIntent(intent))
    }
}
