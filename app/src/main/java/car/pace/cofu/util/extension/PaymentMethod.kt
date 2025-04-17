package car.pace.cofu.util.extension

import android.net.Uri
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import cloud.pace.sdk.api.pay.generated.model.PaymentMethod
import cloud.pace.sdk.api.pay.generated.model.PaymentMethods

const val APPLE_PAY = "applepay"
const val GOOGLE_PAY = "googlepay"

val unsupportedPaymentMethods = listOf(APPLE_PAY)

fun PaymentMethods.toPaymentMethodItems() = map { it.toPaymentMethodItem() }

fun PaymentMethod.toPaymentMethodItem() = PaymentMethodItem(
    id = id,
    vendorId = getPaymentMethodVendor().id,
    imageUrl = getPaymentMethodVendor().logo?.href?.let { Uri.parse(it) },
    kind = getPaymentMethodKind().name ?: kind?.capitalize(Locale.current),
    alias = alias ?: identificationString
)

data class PaymentMethodItem(
    val id: String,
    val vendorId: String,
    val imageUrl: Uri?,
    val kind: String?,
    val alias: String?
)
