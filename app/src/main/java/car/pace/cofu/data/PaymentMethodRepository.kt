package car.pace.cofu.data

import car.pace.cofu.di.coroutine.ApplicationScope
import car.pace.cofu.util.PaymentMethodItem
import car.pace.cofu.util.PaymentMethodUtils
import car.pace.cofu.util.PaymentMethodUtils.GOOGLE_PAY
import car.pace.cofu.util.PaymentMethodUtils.toMethodItems
import car.pace.cofu.util.PaymentMethodUtils.unsupportedPaymentMethods
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.pay.PayAPI.paymentMethods
import cloud.pace.sdk.api.pay.generated.request.paymentMethods.GetPaymentMethodsAPI.getPaymentMethods
import cloud.pace.sdk.appkit.pay.GooglePayUtils.isReadyToPay
import com.google.android.gms.wallet.PaymentsClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import retrofit2.await

@Singleton
class PaymentMethodRepository @Inject constructor(
    @ApplicationScope private val externalScope: CoroutineScope,
    private val paymentsClient: PaymentsClient
) {

    private val _paymentMethods = MutableStateFlow<Result<List<PaymentMethodItem>>?>(null)
    val paymentMethods = _paymentMethods
        .filterNotNull()
        .shareIn(
            scope = externalScope,
            started = SharingStarted.Lazily,
            replay = 1
        )

    suspend fun getPaymentMethods(refresh: Boolean): Result<List<PaymentMethodItem>>? {
        if (refresh) {
            externalScope.async {
                refresh()
            }.await()
        }
        return _paymentMethods.value
    }

    fun refreshPaymentMethods() {
        externalScope.launch {
            refresh()
        }
    }

    private suspend fun refresh() {
        _paymentMethods.value = runCatching {
            API.paymentMethods.getPaymentMethods().await()
                .filterNot { it.kind in unsupportedPaymentMethods }
                .toMethodItems()
                .toMutableList()
                .apply {
                    val googlePayVendorId = "2da0bb36-8534-538b-96f6-a1a905a4f8f8" // TODO: vendor ID?
                    if (none { it.vendorId == googlePayVendorId }) {
                        // Check if Google Pay is available if it is not returned by the Pay API
                        val isAvailable = paymentsClient.isReadyToPay()
                        if (isAvailable) {
                            val googlePay = PaymentMethodItem(
                                id = GOOGLE_PAY,
                                vendorId = googlePayVendorId,
                                imageUrl = PaymentMethodUtils.logoUrl("https://cdn.dev.pace.cloud/pay/payment-method-vendors/googlepay.png"), // TODO: url
                                kind = GOOGLE_PAY,
                                alias = null
                            )

                            add(googlePay)
                        }
                    }
                }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.alias.orEmpty() })
        }
    }
}
