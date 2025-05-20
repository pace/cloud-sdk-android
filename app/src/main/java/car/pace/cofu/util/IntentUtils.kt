package car.pace.cofu.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider
import car.pace.cofu.R
import cloud.pace.sdk.poikit.poi.GasStation
import java.io.File

object IntentUtils {

    const val CHROME_PACKAGE = "com.android.chrome"

    fun startNavigation(context: Context, gasStation: GasStation): Result<Unit> {
        return try {
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
            Result.success(Unit)
        } catch (e: Exception) {
            LogAndBreadcrumb.e(e, "Start Navigation", "Could not launch navigation app")
            Result.failure(e)
        }
    }

    @Suppress("DEPRECATION")
    fun openBiometricSettings(context: Context): Result<Unit> {
        return try {
            val intent = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> Intent(Settings.ACTION_FINGERPRINT_ENROLL)
                else -> Intent(Settings.ACTION_SECURITY_SETTINGS)
            }
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun openLocationSettings(context: Context): Result<Unit> {
        return try {
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
            LogAndBreadcrumb.i(LogAndBreadcrumb.CUSTOM_TAB, "Launch custom tab: $externalUrl")
            true
        } catch (e: ActivityNotFoundException) {
            LogAndBreadcrumb.e(e, LogAndBreadcrumb.CUSTOM_TAB, "No supported browser installed to launch the following URL in a custom tab: $externalUrl")
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
     */
    private fun openAppListing(context: Context) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$CHROME_PACKAGE")))
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$CHROME_PACKAGE")))
        }
    }

    /**
     * Starts the app system settings activity in a new task.
     *
     * @param context The context to start the settings activity.
     */
    fun openAppSettings(context: Context): Result<Unit> {
        return try {
            val uri = Uri.fromParts("package", context.packageName, null)
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates an app chooser intent to share a file.
     *
     * @param context The context to use for creating the intent.
     * @param file The file to share.
     *
     * @return An intent that can be used to share the file.
     */
    fun getShareFileIntent(context: Context, file: File): Intent {
        val fileUri = FileProvider.getUriForFile(context, context.packageName, file)
        return Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(fileUri, context.contentResolver.getType(fileUri))
            putExtra(Intent.EXTRA_STREAM, fileUri)
        }
    }
}
