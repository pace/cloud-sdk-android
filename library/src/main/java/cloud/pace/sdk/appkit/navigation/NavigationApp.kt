package cloud.pace.sdk.appkit.navigation

/**
 * Enum which holds the most common navigation apps.
 */
enum class NavigationApp(val packageName: String, val intentUri: String) {
    GOOGLE_MAPS("com.google.android.apps.maps", "google.navigation:q=%s,%s"),
    WAZE("com.waze", "waze://?ll=%s, %s&navigate=yes")
}
