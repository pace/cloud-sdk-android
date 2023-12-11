package car.pace.cofu.data

import car.pace.cofu.di.coroutine.ApplicationScope
import car.pace.cofu.util.extension.GOOGLE_PAY
import car.pace.cofu.util.extension.PaymentMethodItem
import car.pace.cofu.util.extension.toPaymentMethodItems
import car.pace.cofu.util.extension.unsupportedPaymentMethods
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.pay.PayAPI.paymentMethods
import cloud.pace.sdk.api.pay.generated.request.paymentMethods.GetPaymentMethodsIncludingCreditCheckAPI
import cloud.pace.sdk.api.pay.generated.request.paymentMethods.GetPaymentMethodsIncludingCreditCheckAPI.getPaymentMethodsIncludingCreditCheck
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
            API.paymentMethods.getPaymentMethodsIncludingCreditCheck(GetPaymentMethodsIncludingCreditCheckAPI.Filterstatus.VALID).await()
                .filter {
                    it.kind !in unsupportedPaymentMethods && it.kind != GOOGLE_PAY || paymentsClient.isReadyToPay()
                }
                .toPaymentMethodItems()
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.alias.orEmpty() })
        }
    }
}
