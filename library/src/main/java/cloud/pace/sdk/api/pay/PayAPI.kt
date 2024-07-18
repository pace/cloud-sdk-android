/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay

import cloud.pace.sdk.api.API

/** Welcome to the PACE Payment API documentation.
The PACE Payment API is responsible for managing payment methods for users as well as authorizing payments on behalf of PACE services.
 */
object PayAPI {

    const val VERSION = "2024-3"
    internal val baseUrl = "${API.baseUrl}/pay/$VERSION/"

    class FleetPaymentMethodsAPI
    class NewPaymentMethodsAPI
    class PaymentMethodKindsAPI
    class PaymentMethodsAPI
    class PaymentTokensAPI
    class PaymentTransactionsAPI

    val API.fleetPaymentMethods: FleetPaymentMethodsAPI by lazy { FleetPaymentMethodsAPI() }
    val API.newPaymentMethods: NewPaymentMethodsAPI by lazy { NewPaymentMethodsAPI() }
    val API.paymentMethodKinds: PaymentMethodKindsAPI by lazy { PaymentMethodKindsAPI() }
    val API.paymentMethods: PaymentMethodsAPI by lazy { PaymentMethodsAPI() }
    val API.paymentTokens: PaymentTokensAPI by lazy { PaymentTokensAPI() }
    val API.paymentTransactions: PaymentTransactionsAPI by lazy { PaymentTransactionsAPI() }
}
