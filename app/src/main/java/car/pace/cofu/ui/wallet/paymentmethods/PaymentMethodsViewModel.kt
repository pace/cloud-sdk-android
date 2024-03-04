package car.pace.cofu.ui.wallet.paymentmethods

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.data.PaymentMethodRepository
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_PAYMENT_METHOD_MANAGEMENT_AVAILABLE
import car.pace.cofu.features.analytics.Analytics
import car.pace.cofu.util.LogAndBreadcrumb
import car.pace.cofu.util.UiState
import car.pace.cofu.util.UiState.Loading.toUiState
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.appkit.app.webview.AppWebViewClient
import cloud.pace.sdk.utils.URL
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val paymentMethodRepository: PaymentMethodRepository,
    sharedPreferencesRepository: SharedPreferencesRepository,
    analytics: Analytics
) : ViewModel() {

    private val trackingAppCallback = analytics.TrackingAppCallback()

    val uiState = paymentMethodRepository.paymentMethods
        .onSubscription {
            refresh()
        }
        .map {
            it.toUiState()
        }
        .onEach {
            if (it is UiState.Error) {
                LogAndBreadcrumb.e(it.throwable, LogAndBreadcrumb.PAYMENT_METHODS, "Couldn't load payment methods")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = UiState.Loading
        )

    val canAddPaymentMethods by mutableStateOf(sharedPreferencesRepository.getBoolean(PREF_KEY_PAYMENT_METHOD_MANAGEMENT_AVAILABLE, true))

    fun refresh() {
        paymentMethodRepository.refreshPaymentMethods()
    }

    fun showPaymentMethod(context: Context, id: String) {
        LogAndBreadcrumb.i(LogAndBreadcrumb.PAYMENT_METHODS, "Open Pay PWA to show selected payment method")
        AppKit.openAppActivity(context, paymentMethodUrl(id), true, trackingAppCallback)
    }

    fun addPaymentMethod(context: Context) {
        LogAndBreadcrumb.i(LogAndBreadcrumb.PAYMENT_METHODS, "Open Pay PWA to add a payment method")
        AppKit.openAppActivity(context, paymentMethodCreateUrl(), true, trackingAppCallback)
    }

    private fun paymentMethodUrl(id: String?): String {
        id ?: return URL.payment

        return Uri.parse(URL.payment)
            .buildUpon()
            .appendPath("payment-method")
            .appendPath(id)
            .appendQueryParameter("redirect_uri", AppWebViewClient.CLOSE_URI)
            .build()
            .toString()
    }

    private fun paymentMethodCreateUrl(): String {
        return Uri.parse(URL.payment)
            .buildUpon()
            .appendPath("payment-create")
            .appendQueryParameter("redirect_uri", AppWebViewClient.CLOSE_URI)
            .build()
            .toString()
    }
}
