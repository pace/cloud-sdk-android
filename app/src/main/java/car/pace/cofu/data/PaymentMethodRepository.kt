package car.pace.cofu.data

import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.pay.PayAPI.paymentMethods
import cloud.pace.sdk.api.pay.generated.request.paymentMethods.GetPaymentMethodsAPI.getPaymentMethods
import retrofit2.await
import javax.inject.Inject

class PaymentMethodRepository @Inject constructor() {

    suspend fun getPaymentMethods() = runCatching {
        API.paymentMethods.getPaymentMethods().await()
    }
}
