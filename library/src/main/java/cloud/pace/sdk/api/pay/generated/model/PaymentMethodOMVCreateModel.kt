/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource

@JsonApi(type = "paymentMethod")
class PaymentMethodOMVCreate : Resource() {

    lateinit var kind: Kind

    /* Identifier or PAN (Primary Account Number) representing the OMV Card. The identifier is payment provider specific and provided by the payment provider. */
    lateinit var pan: String

    /* The date the card is expiring in YYMM format. */
    var expiry: String? = null

    /* Indicates whether this payment method should be managed by the creating client, i.e., no other client can modify or delete this method. */
    var managed: Boolean? = null

    /* Track 1 data of payment card. */
    var track1: String? = null

    /* Track 2 data of payment card. */
    var track2: String? = null

    enum class Kind(val value: String) {
        @SerializedName("omv")
        @Json(name = "omv")
        OMV("omv")
    }
}
