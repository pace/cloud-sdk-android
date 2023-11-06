package car.pace.cofu.ui.onboarding.paymentmethod

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import car.pace.cofu.R
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.theme.AppTheme
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.appkit.communication.AppCallbackImpl

@Composable
fun PaymentMethodPage(
    onNext: () -> Unit
) {
    val context = LocalContext.current

    PageScaffold(
        imageRes = R.drawable.ic_payment,
        titleRes = R.string.ONBOARDING_PAYMENT_METHOD_TITLE,
        descriptionRes = R.string.ONBOARDING_PAYMENT_METHOD_DESCRIPTION,
        nextButtonTextRes = R.string.ONBOARDING_ACTIONS_ADD_PAYMENT_METHOD,
        onNextButtonClick = {
            AppKit.openPaymentApp(
                context = context,
                callback = object : AppCallbackImpl() {
                    override fun onClose() = onNext()
                }
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
