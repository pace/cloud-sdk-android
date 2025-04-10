package car.pace.cofu.data

import car.pace.cofu.util.LogAndBreadcrumb
import car.pace.cofu.util.RequestUtils.getHeaders
import car.pace.cofu.util.extension.resume
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.pay.PayAPI.paymentMethodKinds
import cloud.pace.sdk.api.pay.generated.request.paymentMethodKinds.GetPaymentMethodKindsAPI.getPaymentMethodKinds
import cloud.pace.sdk.poikit.utils.ApiException
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.enqueue
import cloud.pace.sdk.utils.requestId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call

@Singleton
class PaymentMethodKindsRepository @Inject constructor(
    private val sharedPreferencesRepository: SharedPreferencesRepository
) {

    data class PaymentMethodKindsResult(
        val twoFactorNeeded: Boolean,
        val paymentMethodManagementEnabled: Boolean
    )

    suspend fun checkPaymentMethodKinds() = getPaymentMethodKinds().fold(
        onSuccess = {
            val twoFactorNeeded = it.any { kind -> kind.twoFactor == true }
            val paymentMethodManagementEnabled = it.any { kind -> kind.managed != true && kind.implicit != true }

            sharedPreferencesRepository.putValue(SharedPreferencesRepository.PREF_KEY_TWO_FACTOR_AVAILABLE, twoFactorNeeded)
            sharedPreferencesRepository.putValue(SharedPreferencesRepository.PREF_KEY_PAYMENT_METHOD_MANAGEMENT_AVAILABLE, paymentMethodManagementEnabled)

            LogAndBreadcrumb.i(LogAndBreadcrumb.PAYMENT_METHOD_KINDS_CHECK, if (twoFactorNeeded) "Two factor authentication enabled" else "Two factor authentication disabled")
            LogAndBreadcrumb.i(LogAndBreadcrumb.PAYMENT_METHOD_KINDS_CHECK, if (paymentMethodManagementEnabled) "Payment method management enabled" else "Payment method management disabled")

            PaymentMethodKindsResult(twoFactorNeeded = twoFactorNeeded, paymentMethodManagementEnabled = paymentMethodManagementEnabled)
        },
        onFailure = {
            // Save the 2fa state as true anyways as we always want to show the settings screen in case of doubt
            sharedPreferencesRepository.putValue(SharedPreferencesRepository.PREF_KEY_TWO_FACTOR_AVAILABLE, true)
            // Save the payment method management state as true as we always want to show the add payment method button in case of doubt
            sharedPreferencesRepository.putValue(SharedPreferencesRepository.PREF_KEY_PAYMENT_METHOD_MANAGEMENT_AVAILABLE, true)

            LogAndBreadcrumb.e(it, LogAndBreadcrumb.PAYMENT_METHOD_KINDS_CHECK, "Payment method kinds check failed")

            PaymentMethodKindsResult(twoFactorNeeded = false, paymentMethodManagementEnabled = false)
        }
    )

    private suspend fun getPaymentMethodKinds() = suspendCancellableCoroutine {
        API.paymentMethodKinds.getPaymentMethodKinds(additionalHeaders = getHeaders()).executeWithRetry(completion = it::resume)
    }

    private fun <T> Call<T>.executeWithRetry(retryCount: Int = 0, completion: (Completion<T>) -> Unit) {
        enqueue {
            onResponse = {
                val body = it.body()
                if (it.isSuccessful && body != null) {
                    completion(Success(body))
                } else {
                    completion(Failure(ApiException(it.code(), it.message(), it.requestId)))
                }
            }

            onFailure = {
                val newRetryCount = retryCount + 1
                if (newRetryCount < MAX_RETRIES) {
                    clone().executeWithRetry(newRetryCount, completion)
                } else {
                    completion(Failure(it ?: Exception("Unknown exception")))
                }
            }
        }
    }

    companion object {
        private const val MAX_RETRIES = 3
    }
}
