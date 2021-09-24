package cloud.pace.sdk.poikit.pricehistory

import java.util.*

data class PriceHistoryFuelType(
    val time: Date,
    val price: Double,
    val currency: String
)
