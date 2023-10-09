package car.pace.cofu.ui.onboarding.twofactor.setup.pin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.ui.component.InputBottomSheet
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun PinSetup(
    viewModel: PinSetupViewModel = hiltViewModel(),
    onDismiss: (result: Result<Unit>) -> Unit
) {
    val bottomSheetType = viewModel.bottomSheetType ?: return

    LaunchedEffect(Unit) {
        viewModel.setupFinished.collect {
            onDismiss(it)
        }
    }

    InputBottomSheet(
        title = stringResource(id = bottomSheetType.titleRes),
        description = stringResource(id = bottomSheetType.descriptionRes),
        buttonText = stringResource(id = bottomSheetType.buttonTextRes),
        errorText = viewModel.errorTextRes?.let { stringResource(id = it) },
        loading = viewModel.loading,
        onDismissRequest = viewModel::next
    )
}

@Preview
@Composable
fun PinSetupPreview() {
    AppTheme {
        PinSetup {}
    }
}
