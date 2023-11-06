package car.pace.cofu.ui.onboarding.twofactor.setup.biometry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.ui.component.InputBottomSheet
import car.pace.cofu.ui.onboarding.twofactor.setup.BiometrySetup
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun BiometrySetup(
    viewModel: BiometrySetupViewModel = hiltViewModel(),
    onDismiss: (result: Result<Unit>) -> Unit
) {
    val errorTextRes = viewModel.errorTextRes

    LaunchedEffect(Unit) {
        viewModel.setupFinished.collect {
            onDismiss(it)
        }
    }

    InputBottomSheet(
        title = stringResource(id = BiometrySetup.titleRes),
        description = stringResource(id = BiometrySetup.descriptionRes),
        buttonText = stringResource(id = BiometrySetup.buttonTextRes),
        errorText = errorTextRes?.let { stringResource(id = it) },
        onDismissRequest = viewModel::next
    )
}

@Preview
@Composable
fun BiometrySetupPreview() {
    AppTheme {
        BiometrySetup {}
    }
}
