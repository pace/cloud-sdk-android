package cloud.pace.sdk.appkit.model

import com.google.android.gms.common.api.Status
import com.google.android.gms.wallet.PaymentData

sealed class PaymentResult {
    data class Ok(val data: PaymentData?) : PaymentResult()
    object Canceled : PaymentResult()
    data class Error(val status: Status?) : PaymentResult()
}
