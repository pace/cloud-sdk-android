package car.pace.cofu.ui.onboarding.authentication

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.R
import car.pace.cofu.ui.component.Description
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun AuthenticationPage(
    viewModel: AuthenticationViewModel = hiltViewModel(),
    onNext: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loginFinished.collect {
            onNext()
        }
    }

    PageScaffold(
        imageVector = Icons.Outlined.Group,
        titleRes = R.string.onboarding_authentication_title,
        nextButtonTextRes = R.string.onboarding_authentication_action,
        onNextButtonClick = {
            viewModel.login(context)
        },
        descriptionContent = {
            Description(
                text = stringResource(id = R.string.onboarding_authentication_description)
            )
        },
        errorText = viewModel.errorText
    )
}

@Preview
@Composable
fun AuthenticationPagePreview() {
    AppTheme {
        AuthenticationPage {}
    }
}
