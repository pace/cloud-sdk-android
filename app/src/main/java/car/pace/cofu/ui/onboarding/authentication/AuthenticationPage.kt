package car.pace.cofu.ui.onboarding.authentication

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import car.pace.cofu.R
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.onboarding.twofactor.biometric.findActivity
import car.pace.cofu.ui.theme.AppTheme
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import kotlinx.coroutines.launch

@Composable
fun AuthenticationPage(
    snackbarHostState: SnackbarHostState,
    onNext: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    fun login() {
        coroutineScope.launch {
            val activity = context.findActivity<AppCompatActivity>()
            when (IDKit.authorize(activity)) {
                is Success -> onNext()
                is Failure -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.ONBOARDING_LOG_IN_UNSUCCESSFUL)
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        login()
                    }
                }
            }
        }
    }

    PageScaffold(
        imageRes = R.drawable.ic_profile,
        titleRes = R.string.ONBOARDING_AUTHENTICATION_TITLE,
        descriptionRes = R.string.ONBOARDING_AUTHENTICATION_DESCRIPTION,
        nextButtonTextRes = R.string.ONBOARDING_ACTIONS_AUTHENTICATE,
        onNextButtonClick = ::login
    )
}

@Preview
@Composable
fun AuthenticationPagePreview() {
    AppTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        AuthenticationPage(snackbarHostState = snackbarHostState) {}
    }
}
