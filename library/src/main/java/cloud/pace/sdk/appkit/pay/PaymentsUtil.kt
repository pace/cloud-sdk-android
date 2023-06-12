package cloud.pace.sdk.appkit.pay

import android.app.Activity
import cloud.pace.sdk.utils.Environment
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants

/**
 * Contains helper static methods for dealing with the Payments API.
 */
object PaymentsUtil {

    const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991

    /**
     * Creates an instance of [PaymentsClient] for use in an [Activity] using the environment set in [WalletConstants].
     *
     * @param activity is the caller's activity.
     * @param environment with which the PACE Cloud SDK was setup.
     */
    fun createPaymentsClient(activity: Activity, environment: Environment): PaymentsClient {
        val payEnvironment = if (environment == Environment.PRODUCTION) WalletConstants.ENVIRONMENT_PRODUCTION else WalletConstants.ENVIRONMENT_TEST
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(payEnvironment)
            .build()

        return Wallet.getPaymentsClient(activity, walletOptions)
    }
}
