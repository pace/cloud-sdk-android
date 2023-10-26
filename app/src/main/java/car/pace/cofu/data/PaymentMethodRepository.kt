package car.pace.cofu.data

import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.pay.PayAPI.paymentMethods
import cloud.pace.sdk.api.pay.generated.request.paymentMethods.GetPaymentMethodsAPI.getPaymentMethods
import javax.inject.Inject
import retrofit2.await

class PaymentMethodRepository @Inject constructor() {

    suspend fun getPaymentMethods() = runCatching {
        API.paymentMethods.getPaymentMethods().await()
    }
}
