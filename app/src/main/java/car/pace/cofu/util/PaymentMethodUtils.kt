package car.pace.cofu.util

import android.content.Context
import android.net.Uri
import car.pace.cofu.R
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.api.pay.generated.model.PaymentMethod
import cloud.pace.sdk.api.pay.generated.model.PaymentMethods
import cloud.pace.sdk.utils.environment
import java.util.Locale

object PaymentMethodUtils {

    private const val CMS_IMAGES_PATH = "cms/images"
    private const val PAY_PATH = "pay"
    private const val PAYMENT_METHOD_VENDORS_PATH = "payment-method-vendors"

    const val AMEX = "amex"
    const val APPLE_PAY = "applepay"
    const val CREDITCARD = "creditcard"
    const val DINERSCLUB = "dinersclub"
    const val DINERS_CLUB = "diners_club"
    const val DKV = "dkv"
    const val GIROPAY = "giropay"
    const val GOOGLE_PAY = "googlepay"
    const val HOYER = "hoyer"
    const val HOYERCARD = "hoyer-app:hoyerCard"
    const val JCB = "jcb"
    const val MAESTRO = "maestro"
    const val MASTERCARD = "mastercard"
    const val PACEPAY = "pacePay"
    const val PAYDIREKT = "paydirekt"
    const val PAYPAL = "paypal"
    const val SEPA = "sepa"
    const val VISA = "visa"
    const val ZGM = "zgm"

    val unsupportedPaymentMethods = listOf(APPLE_PAY)

    fun PaymentMethods.toMethodItems() = map { it.toMethodItem() }

    fun PaymentMethod.toMethodItem() = PaymentMethodItem(
        id = id,
        vendorId = getPaymentMethodVendor().id,
        imageUrl = logoUrl(getPaymentMethodVendor().logo?.href),
        kind = kind,
        alias = alias ?: identificationString
    )

    fun logoUrl(href: String?): Uri? {
        href ?: return null

        val uri = Uri.parse(href)
        return when {
            uri.path?.contains(CMS_IMAGES_PATH) == true -> {
                // 1. The href contains cms/images in the path
                val fileName = uri.lastPathSegment
                Uri.parse(PACECloudSDK.environment.cdnUrl).buildUpon().appendPath(PAY_PATH).appendPath(PAYMENT_METHOD_VENDORS_PATH).appendPath(fileName).build()
            }

            uri.isRelative -> {
                // 2. The href is a relative path, e.g. /pay/payment-method-vendors/paypal.png
                val path = uri.path?.trimEnd('/') // Remove trailing slashes, if existing
                Uri.parse(PACECloudSDK.environment.cdnUrl).buildUpon().path(path).build()
            }

            uri.isAbsolute && uri.lastPathSegment != null -> {
                // 3. The href is an absolute path, e.g. https://cdn.pace.cloud/pay/payment-method-vendors/paypal.png
                val path = uri.path?.trimEnd('/') // Remove trailing slashes, if existing
                uri.buildUpon().path(path).build()
            }

            else -> null
        }
    }

    fun name(context: Context, kind: String?): String {
        return when (kind) {
            AMEX, CREDITCARD, DINERSCLUB, DINERS_CLUB, MAESTRO, MASTERCARD, VISA -> context.getString(R.string.payment_method_kind_credit_card)
            DKV -> context.getString(R.string.payment_method_kind_dkv)
            GOOGLE_PAY -> context.getString(R.string.payment_method_kind_googlepay)
            HOYER, HOYERCARD -> context.getString(R.string.payment_method_kind_hoyer)
            JCB -> context.getString(R.string.payment_method_kind_fuel_card)
            GIROPAY, PAYDIREKT -> context.getString(R.string.payment_method_kind_giropay)
            PAYPAL -> context.getString(R.string.payment_method_kind_paypal)
            SEPA -> context.getString(R.string.payment_method_kind_sepa)
            ZGM -> context.getString(R.string.payment_method_kind_zgm)
            else -> kind?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }.orEmpty()
        }
    }
}

data class PaymentMethodItem(
    val id: String,
    val vendorId: String,
    val imageUrl: Uri?,
    val kind: String?,
    val alias: String?
)
