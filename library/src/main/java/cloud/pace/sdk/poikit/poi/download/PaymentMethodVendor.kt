package cloud.pace.sdk.poikit.poi.download

import com.google.gson.annotations.SerializedName

data class PaymentMethodVendor(
    var id: String? = null,
    var slug: String? = null,
    var name: String? = null,
    var logo: PaymentMethodVendorLogo? = null,
    @SerializedName("payment-method-kindId")
    var paymentMethodKindId: String? = null
)

data class PaymentMethodVendorLogo(
    var href: String? = null,
    var variants: PaymentMethodVendorVariants? = null
)

data class PaymentMethodVendorVariants(
    var dark: Dark? = null
)

data class Dark(
    var href: String? = null
)
