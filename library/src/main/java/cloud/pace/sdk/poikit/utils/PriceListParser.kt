package cloud.pace.sdk.poikit.utils

import cloud.pace.sdk.poikit.poi.Price

/**
 * Fault-tolerant parser for gas station price list
 */
object PriceListParser {
    fun parse(input: String): MutableList<Price> {
        val prices: MutableList<Price> = mutableListOf()
        input.split(",").forEach { entry ->
            var productType: String? = null
            var productName: String? = null
            var price: Double? = null
            entry.split(";").forEach {
                it.split("=").let {
                    if (it.size == 2) {
                        when (it[0]) {
                            "pt" -> productType = it[1]
                            "pn" -> productName = it[1]
                            "pv" -> price = it[1].toDoubleOrNull()
                        }
                    }
                }
            }

            productType?.let {
                val fuelType = if (it == "dieselGtl") "dieselPremium" else it

                prices.add(Price(fuelType, productName, price))
            }
        }
        return prices
    }
}
