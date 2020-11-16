package cloud.pace.sdk.appkit.geofences

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import cloud.pace.sdk.R
import cloud.pace.sdk.appkit.utils.NotificationUtils
import cloud.pace.sdk.utils.AppKitKoinComponent
import cloud.pace.sdk.utils.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofencingRequest
import org.koin.core.inject
import java.util.*

abstract class GeofenceManager : AppKitKoinComponent {

    abstract fun enable(locations: List<GeofenceLocation>, callback: (event: GeofencingEvent) -> Unit, setupCallback: (Result<Void>) -> Unit = {})

    // utility shortcut: send a notification on geofence dwell that opens the specified activity on tap
    abstract fun enable(locations: List<GeofenceLocation>, activity: Class<out FragmentActivity>, setupCallback: (Result<Void>) -> Unit = {}, notificationCallback: (String) -> Unit = {})
    abstract fun disable()

    data class GeofenceLocation(
        val lat: Double,
        val lon: Double,
        val radius: Float,
        val id: String = UUID.randomUUID().toString(),
        val tag: String = "" // for debugging
    )

    companion object {
        const val INTENT_TAG = "geofence_notification"
        const val DWELL_DELAY = 1000 * 3
    }
}

class GeofenceManagerImpl : GeofenceManager() {

    private val context: Context by inject()
    private val geofenceCallback: GeofenceCallback by inject()
    private val geofencingClient: GeofencingClient by inject()

    // only ever show one notification at once
    private var lastNotification: Int? = null

    override fun enable(locations: List<GeofenceLocation>, callback: (event: GeofencingEvent) -> Unit, setupCallback: (Result<Void>) -> Unit) {
        if (locations.isEmpty()) return

        geofenceCallback.callback = callback

        val geofenceList = locations.map {
            Geofence.Builder()
                .setRequestId(it.id)
                .setCircularRegion(
                    it.lat, it.lon, it.radius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT or Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(DWELL_DELAY)
                .build()
        }
        val request = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_DWELL)
            addGeofences(geofenceList)
        }.build()

        val pendingIntent = getIntent(context)

        // clear all
        geofencingClient.removeGeofences(pendingIntent)

        try {
            geofencingClient.addGeofences(request, pendingIntent)?.run {
                addOnSuccessListener {
                    Log.d("Added ${locations.size} geofences: ${locations.joinToString { it.id }}")
                    setupCallback(Result.success(it))
                }
                addOnFailureListener {
                    Log.e(it, "Failed adding geofences: ${it.message}")
                    setupCallback(Result.failure(it))
                }
            }
        } catch (e: SecurityException) {
        }
    }

    override fun enable(locations: List<GeofenceLocation>, activity: Class<out FragmentActivity>, setupCallback: (Result<Void>) -> Unit, notificationCallback: (String) -> Unit) {
        enable(
            locations,
            callback = { event ->
                val triggeringGeofenceIDs = event.triggeringGeofences.joinToString { it.requestId }
                Log.d("Triggering geofences = $triggeringGeofenceIDs")
                Log.d("Geofence location: lat = ${event.triggeringLocation.latitude} lon = ${event.triggeringLocation.longitude}")
                if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL &&
                    // only send notification if in background
                    ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED).not()
                ) {
                    Log.d("Geofence DWELL")
                    lastNotification?.let {
                        NotificationUtils.removeNotification(context, it)
                        lastNotification = null
                    }
                    val intent = Intent(context, activity).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        // for recognizing the intent later
                        putExtra(INTENT_TAG, true)
                    }
                    lastNotification = NotificationUtils.sendNotification(
                        context,
                        context.getString(R.string.geofence_notification_title),
                        context.getString(R.string.geofence_notification_text),
                        R.drawable.ic_cofu,
                        intent
                    )

                    notificationCallback(triggeringGeofenceIDs.take(100))
                    Log.d("Geofence notification sent")
                } else if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    Log.d("Geofence ENTER")
                } else if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    Log.d("Geofence EXIT")
                    lastNotification?.let {
                        NotificationUtils.removeNotification(context, it)
                        lastNotification = null
                        Log.d("Geofence notification removed")
                    }
                }
            },
            setupCallback = setupCallback
        )
    }

    override fun disable() {
        geofencingClient.removeGeofences(getIntent(context))
    }

    private fun getIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
