package car.pace.cofu.ui.onboarding

import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.util.decrease
import car.pace.cofu.core.util.increase
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.pay.PayAPI.paymentMethods
import cloud.pace.sdk.api.pay.generated.request.paymentMethods.GetPaymentMethodsAPI.getPaymentMethods
import cloud.pace.sdk.utils.enqueue

class PaymentMethodItemViewModel(parent: OnboardingViewModel) :
    OnboardingItemViewModel(parent) {
    override val imageRes = R.drawable.ic_payment
    override val textRes = R.string.ONBOARDING_PAYMENT_METHOD_DESCRIPTION
    override val titleRes = R.string.ONBOARDING_PAYMENT_METHOD_TITLE
    override val isFuelTypeSelection = false

    private fun setupButton() {
        buttons.clear()

        buttons.add(
            OnboardingButtonViewModel(
                parent = this,
                textRes = R.string.ONBOARDING_ACTIONS_ADD_PAYMENT_METHOD,
                onClick = {
                    parent.handleEvent(SelectPaymentMethodEvent())
                }
            )
        )

        buttons.add(OnboardingPlaceholderButtonViewModel(this))
    }

    override fun onResponse(response: FragmentEvent) {
        if (response is PaymentMethodSelectedEvent) parent.next()
    }

    override fun onInit(skipIfRedundant: Boolean) {
        if (!skipIfRedundant) {
            setupButton()
            return
        }

        parent.loading.increase()

        API.paymentMethods.getPaymentMethods().enqueue {
            onResponse = {
                parent.loading.decrease()
                val body = it.body()
                if (it.isSuccessful && !body.isNullOrEmpty()) {
                    parent.next()
                } else {
                    // in case of errors here, just show the button, there's no harm done
                    setupButton()
                }
            }
            onFailure = {
                parent.loading.decrease()
                // in case of errors here, just show the button, there's no harm done
                setupButton()
            }
        }
    }
}