package car.pace.cofu.ui.onboarding.paymentmethod

import android.content.Context
import androidx.lifecycle.ViewModel
import car.pace.cofu.features.analytics.Analytics
import cloud.pace.sdk.appkit.AppKit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PaymentMethodViewModel @Inject constructor(
    private val analytics: Analytics
) : ViewModel() {

    fun openPaymentMethod(context: Context, onNext: () -> Unit) {
        AppKit.openPaymentApp(
            context = context,
            callback = analytics.TrackingAppCallback {
                onNext()
            }
        )
    }
}
