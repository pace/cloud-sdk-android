package car.pace.cofu.ui.onboarding.permission.notification

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import car.pace.cofu.R
import car.pace.cofu.ui.component.Description
import car.pace.cofu.ui.icon.StreamApps
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.LogAndBreadcrumb

@Composable
fun NotificationPermissionPage(
    onNext: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        LogAndBreadcrumb.i(LogAndBreadcrumb.ONBOARDING, "Manifest.permission.POST_NOTIFICATIONS ${if (it) "is granted" else "is not granted"}")
        onNext()
    }

    PageScaffold(
        imageVector = Icons.Outlined.StreamApps,
        titleRes = R.string.onboarding_notification_permission_title,
        nextButtonTextRes = R.string.common_use_next,
        onNextButtonClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        descriptionContent = {
            Description(
                text = stringResource(id = R.string.onboarding_notification_permission_description)
            )
        }
    )
}

@Preview
@Composable
fun NotificationPermissionPagePreview() {
    AppTheme {
        NotificationPermissionPage {}
    }
}
