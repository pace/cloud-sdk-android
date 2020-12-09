package cloud.pace.sdk.paykit.paymentmethods

data class PaymentMethodsResponse(val data: List<PaymentMethod>)

data class PaymentMethod(val id: String?)
