package car.pace.cofu.ui.more.permissions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import car.pace.cofu.R
import car.pace.cofu.ui.component.DefaultDialog
import car.pace.cofu.ui.component.DefaultListItem
import car.pace.cofu.ui.component.SwitchInfo
import car.pace.cofu.ui.component.TextTopBar
import car.pace.cofu.ui.onboarding.twofactor.biometric.findActivity
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.Constants
import car.pace.cofu.util.IntentUtils

@Composable
fun PermissionsScreen(
    viewModel: PermissionsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val activity = LocalContext.current.findActivity<AppCompatActivity>()

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        viewModel.checkPermissions()
    }

    PermissionScreenContent(
        items = viewModel.items,
        permissionDialog = viewModel.permissionsDialog,
        permissionRequestFinished = viewModel::permissionRequestFinished,
        isLocationPermissionGiven = viewModel.isLocationPermissionGiven,
        isNotificationPermissionGiven = viewModel.isNotificationPermissionGiven,
        onEnableLocation = {
            viewModel.enableLocation(activity, it)
        },
        onEnableNotifications = {
            viewModel.enableNotifications(activity, it)
        },
        onNavigateUp = onNavigateUp
    )
}

@Composable
fun PermissionScreenContent(
    items: List<PermissionsViewModel.PermissionsItem>,
    permissionDialog: PermissionsViewModel.PermissionDialog?,
    isLocationPermissionGiven: Boolean,
    isNotificationPermissionGiven: Boolean,
    permissionRequestFinished: (Boolean) -> Unit,
    onEnableLocation: (Boolean) -> Unit,
    onEnableNotifications: (Boolean) -> Unit,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissionRequestFinished(true)
    }

    Column {
        TextTopBar(
            text = stringResource(id = R.string.menu_items_permissions),
            onNavigateUp = onNavigateUp
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            items(
                items = items,
                key = PermissionsViewModel.PermissionsItem::id,
                contentType = { Constants.DEFAULT_LIST_ITEM_CONTENT_TYPE }
            ) { item ->
                DefaultListItem(
                    icon = item.icon,
                    title = context.getString(item.title),
                    description = context.getString(item.description),
                    switchInfo = when (item) {
                        PermissionsViewModel.PermissionsItem.LOCATION -> {
                            SwitchInfo(isLocationPermissionGiven) {
                                onEnableLocation(it)
                            }
                        }

                        PermissionsViewModel.PermissionsItem.NOTIFICATIONS -> {
                            SwitchInfo(isNotificationPermissionGiven) {
                                onEnableNotifications(it)
                            }
                        }
                    }
                )
            }
        }
    }

    when (permissionDialog) {
        is PermissionsViewModel.PermissionDialog.SettingsDialog -> {
            DefaultDialog(
                title = context.getString(permissionDialog.text),
                confirmButtonText = context.getString(R.string.ALERT_LOCATION_PERMISSION_ACTIONS_OPEN_SETTINGS),
                dismissButtonText = context.getString(R.string.common_use_cancel),
                onConfirm = {
                    IntentUtils.openAppSettings(context)
                    permissionRequestFinished(false)
                },
                onDismiss = {
                    permissionRequestFinished(false)
                }
            )
        }

        is PermissionsViewModel.PermissionDialog.SystemDialog -> {
            launcher.launch(permissionDialog.permissions.toTypedArray())
        }

        else -> {}
    }
}

@Preview
@Composable
fun PermissionsScreenContentPreview() {
    AppTheme {
        PermissionScreenContent(
            items = listOf(PermissionsViewModel.PermissionsItem.NOTIFICATIONS, PermissionsViewModel.PermissionsItem.LOCATION),
            permissionDialog = null,
            isLocationPermissionGiven = false,
            isNotificationPermissionGiven = true,
            permissionRequestFinished = {},
            onEnableLocation = {},
            onEnableNotifications = {},
            onNavigateUp = {}
        )
    }
}

@Preview
@Composable
fun PermissionsScreenDialogPreview() {
    AppTheme {
        PermissionScreenContent(
            items = listOf(PermissionsViewModel.PermissionsItem.NOTIFICATIONS, PermissionsViewModel.PermissionsItem.LOCATION),
            permissionDialog = PermissionsViewModel.PermissionDialog.SettingsDialog(R.string.alert_notification_permission_disabled_title),
            isLocationPermissionGiven = false,
            isNotificationPermissionGiven = true,
            permissionRequestFinished = {},
            onEnableLocation = {},
            onEnableNotifications = {},
            onNavigateUp = {}
        )
    }
}
