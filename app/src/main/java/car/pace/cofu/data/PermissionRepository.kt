package car.pace.cofu.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_NOTIFICATION_PERMISSION_REQUESTED
import car.pace.cofu.util.BuildProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferencesRepository: SharedPreferencesRepository
) {
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun shouldShowRequestPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    @SuppressLint("InlinedApi")
    fun canRequestNotificationPermission(): Boolean {
        return BuildProvider.isAnalyticsEnabled() &&
            BuildProvider.getSDKVersion() >= Build.VERSION_CODES.TIRAMISU &&
            !sharedPreferencesRepository.getBoolean(PREF_KEY_NOTIFICATION_PERMISSION_REQUESTED, false) &&
            !isPermissionGranted(NOTIFICATION_PERMISSION)
    }

    companion object {

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        const val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS
        const val FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
        const val COARSE_LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION

        val locationPermissions = listOf(FINE_LOCATION_PERMISSION, COARSE_LOCATION_PERMISSION)
    }
}
