/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.model

import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.Date

@JsonApi(type = "priceHistory")
class PriceHistory : Resource() {

    /* Currency as specified in ISO-4217. */
    var currency: String? = null

    /* Beginning of time interval */
    var from: Date? = null
    var fuelPrices: List<FuelPrices>? = null
    var productName: String? = null

    /* End of time interval */
    var to: Date? = null

    class FuelPrices {

        /* The datetime of the price value */
        var at: Date? = null

        /* The price at this point in time */
        var price: Double? = null
    }
}
