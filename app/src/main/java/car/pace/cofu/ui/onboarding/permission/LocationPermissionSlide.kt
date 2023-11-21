package car.pace.cofu.ui.onboarding.permission

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import car.pace.cofu.R
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.extension.locationPermissions

@Composable
fun LocationPermissionPage(
    onNext: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        onNext()
    }

    PageScaffold(
        imageRes = R.drawable.ic_location,
        titleRes = R.string.onboarding_permission_title,
        descriptionRes = R.string.onboarding_permission_description,
        nextButtonTextRes = R.string.onboarding_permission_action,
        onNextButtonClick = {
            launcher.launch(locationPermissions)
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
