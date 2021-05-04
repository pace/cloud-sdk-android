/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import moe.banana.jsonapi2.HasMany
import moe.banana.jsonapi2.HasOne
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.*

class PaymentMethodPayPalCreateBody {

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
        /* URL that the user is redirected to after successfully creating the payment method in the backend. Must be provided if the backend should create the Billing Agreement. */
        lateinit var successURL: String
        /* URL that the user is redirected to after creating the payment method in the backend failes. Must be provided if the backend should create the Billing Agreement. */
        lateinit var failureURL: String
        /* URL that the user is redirected to after creating the payment method in the backend was canceled by the user. Must be provided if the backend should create the Billing Agreement. */
        lateinit var canceledURL: String

        enum class Kind(val value: String) {
            @SerializedName("paypal")
            @Json(name = "paypal")
            PAYPAL("paypal")
        }
    }
}
