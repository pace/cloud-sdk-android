package car.pace.cofu.data

import car.pace.cofu.util.LogAndBreadcrumb
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

    suspend fun check2FAState() = getPaymentMethodKinds().fold(
        onSuccess = {
            val twoFactorNeeded = it.any { kind -> kind.twoFactor == true }
            sharedPreferencesRepository.putValue(SharedPreferencesRepository.PREF_KEY_TWO_FACTOR_AVAILABLE, twoFactorNeeded)
            LogAndBreadcrumb.i(LogAndBreadcrumb.TWO_FACTOR_CHECK, if (twoFactorNeeded) "Two factor authentication enabled" else "Two factor authentication disabled")
            twoFactorNeeded
        },
        onFailure = {
            // Save the 2fa state as true anyways as we always want to show the settings screen in case of doubt
            sharedPreferencesRepository.putValue(SharedPreferencesRepository.PREF_KEY_TWO_FACTOR_AVAILABLE, true)
            LogAndBreadcrumb.e(it, LogAndBreadcrumb.TWO_FACTOR_CHECK, "Two factor check failed")
            false
        }
    )

    private suspend fun getPaymentMethodKinds() = suspendCancellableCoroutine {
        API.paymentMethodKinds.getPaymentMethodKinds().executeWithRetry(completion = it::resume)
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
