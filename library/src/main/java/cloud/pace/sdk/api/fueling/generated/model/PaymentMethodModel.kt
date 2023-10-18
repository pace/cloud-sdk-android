/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.fueling.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource

@JsonApi(type = "paymentMethod")
class PaymentMethod : Resource() {

    var meta: Meta? = null

    /* Customer chosen alias for the payment method */
    var alias: String? = null
    var identificationString: String? = null

    /* one of sepa, creditcard, paypal, paydirekt, dkv, applepay, ... */
    var kind: String? = null
    var mandatoryAuthorisationAttributes: List<MandatoryAuthorisationAttributes>? = null

    /* Identifies if the payment method is a PACE payment method (`true`) or a broker method (`false`) */
    var pacePay: Boolean? = null

    /* The desired status for a payment method is `verified`, this means the method is ready to use.
A payment method that has the status `created` has yet to be verified. This is the case for payment methods,
which have an asynchronous verification process, e.g., paydirekt (waiting for an email).
 */
    var status: Status? = null

    /* indicates if the payment method kind requires two factors later on */
    var twoFactor: Boolean? = null

    /* PACE resource name(s) to payment method vendor */
    var vendorPRN: String? = null

    /* The desired status for a payment method is `verified`, this means the method is ready to use.
    A payment method that has the status `created` has yet to be verified. This is the case for payment methods,
    which have an asynchronous verification process, e.g., paydirekt (waiting for an email).
     */
    enum class Status(val value: String) {
        @SerializedName("created")
        @Json(name = "created")
        CREATED("created"),

        @SerializedName("verified")
        @Json(name = "verified")
        VERIFIED("verified"),

        @SerializedName("pending")
        @Json(name = "pending")
        PENDING("pending"),

        @SerializedName("unacceptable")
        @Json(name = "unacceptable")
        UNACCEPTABLE("unacceptable")
    }

    /* Mandatory transaction attribute validator */
    class MandatoryAuthorisationAttributes {

        var maxLength: Int? = null
        var name: String? = null
        var regex: String? = null
    }

    class Meta {

        /* Merchant name if the request was made in a way that a merchant name can be determined. For example if requesting payment methods for a specific gas station, it is the merchant name at that gas station. */
        var merchantName: String? = null
    }
}