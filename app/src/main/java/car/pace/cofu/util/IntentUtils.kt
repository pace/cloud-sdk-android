package car.pace.cofu.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import car.pace.cofu.R
import cloud.pace.sdk.poikit.poi.GasStation
import timber.log.Timber

object IntentUtils {

    const val CHROME_PACKAGE = "com.android.chrome"

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

    /**
     * Loads the [url] in a Custom Tabs Activity.
     * Catches an [ActivityNotFoundException] if no supported browser is installed or enabled
     * and shows a dialog prompting the user to install Google Chrome.
     *
     * @param context The context to start the Custom Tabs Activity.
     *
     * @return True if the URL could be loaded in a Custom Tabs Activity, false otherwise.
     */
    fun launchInCustomTabIfAvailable(context: Context, url: String?): Boolean {
        val externalUrl = url ?: return false
        return try {
            getCustomTabsIntent().launchUrl(context, Uri.parse(externalUrl))
            true
        } catch (e: ActivityNotFoundException) {
            Timber.i(e, "No supported browser installed to launch the following URL in a custom tab: $externalUrl")
            showNoSupportedBrowserDialog(context)
            false
        }
    }

    /**
     * Returns a [CustomTabsIntent] with default settings.
     */
    private fun getCustomTabsIntent() = CustomTabsIntent.Builder().build()

    private fun showNoSupportedBrowserDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.no_supported_browser_dialog_title))
            .setMessage(context.getString(R.string.no_supported_browser_toast))
            .setNeutralButton(context.getString(R.string.common_use_open_play_store)) { dialog, _ ->
                openAppListing(context)
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Opens the specified app listing page in the Play Store.
     *
     * @param context The context to start the Play Store activity.
     * @param packageName The package name of the app to open in the Play Store. Defaults to `car.pace.drive`.
     */
    private fun openAppListing(context: Context) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$CHROME_PACKAGE")))
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$CHROME_PACKAGE")))
        }
    }
}
