/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.fueling.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import moe.banana.jsonapi2.HasMany
import moe.banana.jsonapi2.HasOne
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.*

@JsonApi(type = "processPaymentResponse")
class ProcessPaymentResponse : Resource() {

    var vat: VAT? = null
    /* Currency as specified in ISO-4217. */
    var currency: String? = null
    var gasStationId: String? = null
    /* Mileage in meters */
    var mileage: Int? = null
    var paymentToken: String? = null
    var priceIncludingVAT: Double? = null
    var priceWithoutVAT: Double? = null
    var pumpId: String? = null
    /* Vehicle identification number */
    var vin: String? = null

    class VAT {

        var amount: Double? = null
        var rate: Double? = null
    }

}
