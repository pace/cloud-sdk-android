package car.pace.cofu.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import cloud.pace.sdk.poikit.poi.GasStation
import timber.log.Timber

object IntentUtils {

    fun startNavigation(context: Context, gasStation: GasStation) {
        try {
            // If Google Maps is installed, launch navigation directly
            val uri = Uri.parse("google.navigation:q=${gasStation.latitude},${gasStation.longitude}")
            val mapsIntent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps")
            val activities = context.packageManager.queryIntentActivities(mapsIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (activities.size > 0) {
                context.startActivity(mapsIntent)
            } else {
                // No Google Maps installed - fallback to regular geo URI
                val intent = Intent(Intent.ACTION_VIEW)
                val address = gasStation.address?.let { address ->
                    "${address.street} ${address.houseNumber}, ${address.postalCode} ${address.city}"
                }
                intent.data = Uri.parse("geo:${gasStation.latitude},${gasStation.longitude}?q=$address")
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Timber.e(e, "Could not launch navigation app")
        }
    }
}
