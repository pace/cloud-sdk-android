package car.pace.cofu.ui.onboarding.paymentmethod

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.R
import car.pace.cofu.ui.component.Description
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun PaymentMethodPage(
    viewModel: PaymentMethodViewModel = hiltViewModel(),
    onNext: () -> Unit
) {
    val context = LocalContext.current

    PageScaffold(
        imageVector = Icons.Outlined.AddCard,
        titleRes = R.string.onboarding_payment_method_title,
        nextButtonTextRes = R.string.onboarding_payment_method_action,
        onNextButtonClick = {
            viewModel.openPaymentMethod(context, onNext)
        },
        descriptionContent = {
            Description(
                text = stringResource(id = R.string.onboarding_payment_method_description)
            )
        }
    )
}

@Preview
@Composable
fun PaymentMethodPagePreview() {
    AppTheme {
        PaymentMethodPage {}
    }
}
