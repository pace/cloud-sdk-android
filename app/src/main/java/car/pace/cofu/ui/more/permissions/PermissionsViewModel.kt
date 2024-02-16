package car.pace.cofu.ui.more.permissions

import android.os.Build
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkUnreadChatAlt
import androidx.compose.material.icons.outlined.PersonPinCircle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import car.pace.cofu.R
import car.pace.cofu.data.PermissionRepository
import car.pace.cofu.util.BuildProvider
import car.pace.cofu.util.extension.locationPermissions
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val permissionRepository: PermissionRepository
) : ViewModel() {

    var isLocationPermissionGiven by mutableStateOf(permissionRepository.isPermissionGranted(PermissionRepository.LOCATION_PERMISSION))
    var isNotificationPermissionGiven by mutableStateOf(permissionRepository.isPermissionGranted(PermissionRepository.NOTIFICATION_PERMISSION))
    var permissionsDialog by mutableStateOf<PermissionDialog?>(null)

    sealed class PermissionDialog {
        data class SettingsDialog(@StringRes val text: Int) : PermissionDialog()

        data class SystemDialog(val permissions: Array<String>) : PermissionDialog()
    }

    enum class PermissionsItem(
        val id: String = UUID.randomUUID().toString(),
        @StringRes val title: Int,
        @StringRes val description: Int,
        val icon: ImageVector
    ) {
        NOTIFICATIONS(
            title = R.string.menu_permissions_notifications_title,
            description = R.string.onboarding_notification_permission_description,
            icon = Icons.Outlined.MarkUnreadChatAlt
        ),
        LOCATION(
            title = R.string.menu_permissions_location_title,
            description = R.string.onboarding_location_permission_description,
            icon = Icons.Outlined.PersonPinCircle
        )
    }

    val items = buildList {
        if (BuildProvider.isAnalyticsEnabled() && BuildProvider.getSDKVersion() >= Build.VERSION_CODES.TIRAMISU) {
            add(PermissionsItem.NOTIFICATIONS)
        }

        add(PermissionsItem.LOCATION)
    }

    fun enableNotifications(activity: AppCompatActivity, enable: Boolean) {
        permissionsDialog = when {
            !enable -> PermissionDialog.SettingsDialog(R.string.alert_notification_permission_disabled_title)
            enable && permissionRepository.shouldShowRequestPermissionRationale(activity, PermissionRepository.NOTIFICATION_PERMISSION) -> {
                PermissionDialog.SystemDialog(arrayOf(PermissionRepository.NOTIFICATION_PERMISSION))
            }
            else -> PermissionDialog.SettingsDialog(R.string.alert_notification_permission_denied_title)
        }
    }

    fun enableLocation(activity: AppCompatActivity, enable: Boolean) {
        permissionsDialog = when {
            !enable -> PermissionDialog.SettingsDialog(R.string.alert_location_permission_disabled_title)
            enable && permissionRepository.shouldShowRequestPermissionRationale(activity, PermissionRepository.LOCATION_PERMISSION) -> {
                PermissionDialog.SystemDialog(locationPermissions)
            }
            else -> PermissionDialog.SettingsDialog(R.string.alert_location_permission_denied_title)
        }
    }

    fun permissionRequestFinished(checkPermissions: Boolean) {
        permissionsDialog = null
        if (checkPermissions) {
            checkPermissions()
        }
    }

    fun checkPermissions() {
        isLocationPermissionGiven = permissionRepository.isPermissionGranted(PermissionRepository.LOCATION_PERMISSION)
        isNotificationPermissionGiven = permissionRepository.isPermissionGranted(PermissionRepository.NOTIFICATION_PERMISSION)
    }
}
