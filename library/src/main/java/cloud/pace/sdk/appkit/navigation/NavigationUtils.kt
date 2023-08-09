package cloud.pace.sdk.appkit.navigation

import android.content.Intent
import android.net.Uri
import android.os.Parcelable

object NavigationUtils {

    /**
     * Returns a chooser intent to let the user choose between Google Maps or Waze (if installed)
     * to start the navigation to the provided location.
     *
     * @param lat The latitude of the location to be navigated to.
     * @param lon The longitude of the location to be navigated to.
     *
     * @return A navigation app chooser intent.
     */
    fun getNavigationIntent(lat: Double, lon: Double): Intent {
        val targetIntents = arrayListOf(
            getNavigationAppIntent(NavigationApp.GOOGLE_MAPS, lat, lon),
            getNavigationAppIntent(NavigationApp.WAZE, lat, lon)
        )

        return Intent.createChooser(targetIntents.removeAt(0), null)
            .putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toArray(arrayOfNulls<Parcelable>(targetIntents.size)))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /**
     * Builds a NavigationApp intent for the given app.
     *
     * @param lat The latitude of the location to be navigated to.
     * @param lon The longitude of the location to be navigated to.
     *
     * @return The intent for the navigation
     */
    private fun getNavigationAppIntent(navigationApp: NavigationApp, lat: Double, lon: Double): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(String.format(navigationApp.intentUri, lat, lon)))
            .setPackage(navigationApp.packageName)
    }
}
