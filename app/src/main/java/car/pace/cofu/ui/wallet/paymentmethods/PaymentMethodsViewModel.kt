package car.pace.cofu.ui.wallet.paymentmethods

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.data.PaymentMethodRepository
import car.pace.cofu.util.LogAndBreadcrumb
import car.pace.cofu.util.UiState
import car.pace.cofu.util.UiState.Loading.toUiState
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
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

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

    fun refresh() {
        paymentMethodRepository.refreshPaymentMethods()
    }

    fun paymentMethodUrl(id: String?): String {
        id ?: return URL.payment

        return Uri.parse(URL.payment)
            .buildUpon()
            .appendPath("payment-method")
            .appendPath(id)
            .appendQueryParameter("redirect_uri", AppWebViewClient.CLOSE_URI)
            .build()
            .toString()
    }

    fun paymentMethodCreateUrl(): String {
        return Uri.parse(URL.payment)
            .buildUpon()
            .appendPath("payment-create")
            .appendQueryParameter("redirect_uri", AppWebViewClient.CLOSE_URI)
            .build()
            .toString()
    }
}
