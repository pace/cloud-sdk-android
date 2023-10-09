package car.pace.cofu.ui.onboarding.permission

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import car.pace.cofu.R
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun LocationPermissionPage(
    onNext: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        onNext()
    }

    PageScaffold(
        imageRes = R.drawable.ic_location,
        titleRes = R.string.ONBOARDING_PERMISSION_TITLE,
        descriptionRes = R.string.ONBOARDING_PERMISSION_DESCRIPTION,
        nextButtonTextRes = R.string.ONBOARDING_ACTIONS_SHARE_LOCATION,
        onNextButtonClick = {
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            launcher.launch(permissions)
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
