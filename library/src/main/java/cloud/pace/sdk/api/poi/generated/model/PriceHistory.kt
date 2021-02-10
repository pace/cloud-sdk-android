/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import moe.banana.jsonapi2.HasMany
import moe.banana.jsonapi2.HasOne
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.*

@JsonApi(type = "priceHistory")
class PriceHistory : Resource() {

    var currency: Currency? = null
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
