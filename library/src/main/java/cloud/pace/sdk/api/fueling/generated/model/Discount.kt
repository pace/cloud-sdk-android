/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.fueling.generated.model

import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource

@JsonApi(type = "discount")
class Discount : Resource() {

    /* Amount in the currency of the pumps ready to pay amount */
    var amount: Double? = null

    /* Human readable name of the discount provider */
    var provider: String? = null

    /* Line item */
    var title: String? = null
}