package cloud.pace.sdk.paykit

import cloud.pace.sdk.paykit.paymentmethods.PaymentMethodsApiClient
import cloud.pace.sdk.paykit.paymentmethods.PaymentMethodsResponse
import cloud.pace.sdk.paykit.transactions.TransactionsApiClient
import cloud.pace.sdk.paykit.transactions.TransactionsResponse
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Environment

object PayKit {

    fun getAllReadyToUsePaymentMethods(environment: Environment, accessToken: String, status: String = "valid", completion: (Completion<PaymentMethodsResponse>) -> Unit) {
        PaymentMethodsApiClient(environment, accessToken).getAllReadyToUsePaymentMethods(status, completion)
    }

    fun getTransactions(environment: Environment, accessToken: String, sortBy: String? = "-createdAt", completion: (Completion<TransactionsResponse>) -> Unit) {
        TransactionsApiClient(environment, accessToken).getTransactions(sortBy, completion)
    }
}
