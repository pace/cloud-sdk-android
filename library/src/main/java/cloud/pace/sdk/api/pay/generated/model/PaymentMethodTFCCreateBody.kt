/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

class PaymentMethodTFCCreateBody {

    lateinit var type: Type
    var attributes: Attributes? = null

    /* The ID of this payment method. */
    var id: String? = null

    enum class Type(val value: String) {
        @SerializedName("paymentMethod")
        @Json(name = "paymentMethod")
        PAYMENTMETHOD("paymentMethod")
    }

    class Attributes {

        lateinit var kind: Kind

        /* Identifier representing The Fuel Company Card number. The identifier is payment provider specific and provided by the payment provider.
     */
        var cardNumber: String? = null

        /* The date the card is expiring in YYMM format. */
        var expiry: String? = null

        /* Indicates whether this payment method should be managed by the creating client, i.e., no other client can modify or delete this method. */
        var managed: Boolean? = null

        /* Personal identification number is a security code for verifying the user's identity. */
        var pin: String? = null

        enum class Kind(val value: String) {
            @SerializedName("tfc")
            @Json(name = "tfc")
            TFC("tfc")
        }
    }
}
