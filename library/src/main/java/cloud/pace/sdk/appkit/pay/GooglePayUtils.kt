package cloud.pace.sdk.appkit.pay

import android.app.Activity
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.utils.Environment
import cloud.pace.sdk.utils.environment
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

/**
 * Contains helper static methods for dealing with the Google Pay API.
 */
object GooglePayUtils {

    const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991

    private val PRODUCTION_ALLOWED_AUTH_METHODS = listOf("CRYPTOGRAM_3DS")

    private val TEST_ALLOWED_AUTH_METHODS = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")

    /**
     * Creates an instance of Google Pay's [PaymentsClient] for use in an [Activity] using the environment set in [WalletConstants].
     *
     * @param activity is the caller's activity.
     */
    fun createPaymentsClient(activity: Activity): PaymentsClient {
        val payEnvironment = if (PACECloudSDK.environment == Environment.PRODUCTION) WalletConstants.ENVIRONMENT_PRODUCTION else WalletConstants.ENVIRONMENT_TEST
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(payEnvironment)
            .build()

        return Wallet.getPaymentsClient(activity, walletOptions)
    }

    /**
     * Creates an [IsReadyToPayRequest] which can be uses to check if the user is ready to pay with Google Pay.
     */
    fun getIsReadyToPayRequest(): IsReadyToPayRequest {
        val allowedAuthMethods = JSONArray(if (PACECloudSDK.environment == Environment.PRODUCTION) PRODUCTION_ALLOWED_AUTH_METHODS else TEST_ALLOWED_AUTH_METHODS)
        val json = JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put(
                "allowedPaymentMethods",
                JSONArray().put(
                    JSONObject().apply {
                        put("type", "CARD")
                        put(
                            "parameters",
                            JSONObject().apply {
                                put("allowedAuthMethods", allowedAuthMethods)
                                put("allowedCardNetworks", JSONArray(listOf("MASTERCARD", "VISA")))
                            }
                        )
                    }
                )
            )
        }

        return IsReadyToPayRequest.fromJson(json.toString())
    }

    /**
     * Determines if the user can make payments using the Google Pay API.
     * **Note:** This call creates a new [PaymentsClient]. Consider using the [PaymentsClient.isReadyToPay] extension function on a [PaymentsClient].
     *
     * @param activity is the caller's activity.
     */
    suspend fun isReadyToPay(activity: Activity): Boolean {
        return createPaymentsClient(activity).isReadyToPay()
    }

    /**
     * Determines if the user can make payments using the Google Pay API.
     */
    suspend fun PaymentsClient.isReadyToPay(): Boolean {
        return try {
            val request = getIsReadyToPayRequest()
            isReadyToPay(request).await()
        } catch (e: Exception) {
            Timber.e(e, "Could not check if Google Pay is available")
            false
        }
    }
}
