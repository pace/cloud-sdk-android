/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

class RequestPaymentMethodModelBody {

    lateinit var type: Type
    lateinit var attributes: Attributes

    /* Payment method UUID */
    var id: String? = null

    enum class Type(val value: String) {
        @SerializedName("paymentMethod")
        @Json(name = "paymentMethod")
        PAYMENTMETHOD("paymentMethod")
    }

    class Attributes {

        /* Point of Interest ID */
        var poiID: String? = null
    }
}
