package car.pace.cofu.ui.onboarding.permission.location

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import car.pace.cofu.R
import car.pace.cofu.data.PermissionRepository.Companion.locationPermissions
import car.pace.cofu.ui.component.Description
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.LogAndBreadcrumb

@Composable
fun LocationPermissionPage(
    onNext: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        it.forEach { (permission, isGranted) ->
            LogAndBreadcrumb.i(LogAndBreadcrumb.ONBOARDING, "$permission ${if (isGranted) "is granted" else "is not granted"}")
        }
        onNext()
    }

    PageScaffold(
        imageVector = Icons.Outlined.TravelExplore,
        titleRes = R.string.onboarding_location_permission_title,
        nextButtonTextRes = R.string.onboarding_location_permission_action,
        onNextButtonClick = {
            launcher.launch(locationPermissions.toTypedArray())
        },
        descriptionContent = {
            Description(
                text = stringResource(id = R.string.onboarding_location_permission_description)
            )
        }
    )
}

@Preview
@Composable
fun LocationPermissionPagePreview() {
    AppTheme {
        LocationPermissionPage {}
    }
}
