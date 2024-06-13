package car.pace.cofu.ui.onboarding.paymentmethod

import android.content.Context
import androidx.lifecycle.ViewModel
import car.pace.cofu.data.analytics.Analytics
import car.pace.cofu.util.extension.paymentMethodCreate
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.utils.URL
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PaymentMethodViewModel @Inject constructor(
    private val analytics: Analytics
) : ViewModel() {

    fun openPaymentMethod(context: Context, onNext: () -> Unit) {
        AppKit.openAppActivity(
            context = context,
            url = URL.paymentMethodCreate,
            callback = analytics.TrackingAppCallback(onNext)
        )
    }
}
