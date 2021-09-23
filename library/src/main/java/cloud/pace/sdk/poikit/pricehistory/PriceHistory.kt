package cloud.pace.sdk.poikit.pricehistory

import com.squareup.moshi.Json
import java.util.*

data class PriceHistory(
    @Json(name = "fuel_type")
    val fuelType: String,
    val prices: List<Price>
)

data class Price(
    val currency: String,
    val data: List<Data>
)

data class Data(
    val time: Date,
    val price: Double
)
