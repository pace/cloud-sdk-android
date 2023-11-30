package car.pace.cofu.ui.onboarding.authentication

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import car.pace.cofu.R
import car.pace.cofu.ui.component.Description
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.onboarding.twofactor.biometric.findActivity
import car.pace.cofu.ui.theme.AppTheme
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import kotlinx.coroutines.launch

@Composable
fun AuthenticationPage(
    onNext: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var errorText: String? by remember { mutableStateOf(null) }

    fun login() {
        coroutineScope.launch {
            val activity = context.findActivity<AppCompatActivity>()
            when (IDKit.authorize(activity)) {
                is Success -> onNext()
                is Failure -> errorText = context.getString(R.string.onboarding_login_unsuccessful)
            }
        }
    }

    PageScaffold(
        imageVector = Icons.Outlined.Group,
        titleRes = R.string.onboarding_authentication_title,
        nextButtonTextRes = R.string.onboarding_authentication_action,
        onNextButtonClick = ::login,
        descriptionContent = {
            Description(
                text = stringResource(id = R.string.onboarding_authentication_description)
            )
        },
        errorText = errorText
    )
}

@Preview
@Composable
fun AuthenticationPagePreview() {
    AppTheme {
        AuthenticationPage {}
    }
}
