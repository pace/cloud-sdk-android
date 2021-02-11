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

@JsonApi(type = "paymentToken")
class PaymentToken : Resource() {

    /* The amount that this token represents. */
    var amount: Double? = null
    var currency: Currency? = null
    /* PACE resource name(s) of one or multiple resources, for which the payment was authorized. */
    var purposePRNs: List<String>? = null
    /* The datetime (iso8601) after which the token is no longer valid. May not be provided. */
    var validUntil: Date? = null
    /* paymentToken value. Format might change (externally provided - by payment provider) */
    var value: String? = null

    private var paymentMethod: HasOne<PaymentMethodRelationship> = HasOne()
    fun getPaymentMethod() = paymentMethod.get(document)


}