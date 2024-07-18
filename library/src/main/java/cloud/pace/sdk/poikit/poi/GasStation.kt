package cloud.pace.sdk.poikit.poi

import cloud.pace.sdk.poikit.geo.CofuGasStation
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_AMENITIES
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_BRAND
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_BRAND_ID
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_CURRENCY
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_FOODS
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_LOYALTY_PROGRAMS
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_OPENING_HOURS
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_PACE_CONNECTED_FUELING
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_PAYMENT_METHODS
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_POI_NAME
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_POSTAL_SERVICES
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_PRICE_COMPARISON_OPT_OUT
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_PRICE_FORMAT
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_PRICE_LIST
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_SERVICES
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_SHOP_GOODS
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_VALID_FROM
import cloud.pace.sdk.poikit.utils.OpeningHoursParser
import cloud.pace.sdk.poikit.utils.PriceListParser
import cloud.pace.sdk.utils.OpeningHoursUtils
import java.util.*

/**
 * Point of Interest - Gas station with opening hours, prices and payment methods.
 */
open class GasStation(id: String, geometry: ArrayList<Geometry.CommandGeo>) :
    PointOfInterest(id, geometry) {

    override val poiLayer = POILayer.GAS_STATION

    /**
     * Name of the gas station.
     */
    var name: String? = null

    /**
     * Brand of the gas station
     */
    var brand: String? = null

    /**
     * Brand id
     */
    var brandId: String? = null

    /**
     * Opening hours represented by a list of [OpeningHours] rules.
     */
    var openingHours: List<OpeningHours> = listOf()

    /**
     * [Price] list.
     */
    var prices: MutableList<Price> = mutableListOf()

    /**
     * Accepted currency, code in ISO 4217
     */
    var currency: String? = null

    /**
     * Format string string indicating which digits should be used and how they are formatted, e.g. d.dds
     */
    var priceFormat: String? = null

    /**
     * Valid from (UTC UNIX timestamp) since when the last update happened on the POI
     */
    var validFrom: Date? = null

    /**
     * List of [PaymentMethod]s
     */
    var paymentMethods: MutableList<String> = mutableListOf()

    /**
     * Specifies the availability of at least one payment provider for PACE Connected Fueling
     */
    var isConnectedFuelingAvailable: Boolean? = null

    /**
     * `POIKit.CofuGasStation` object of this gas station
     *  Null if gas station is no cofu station
     */
    var cofuGasStation: CofuGasStation? = null

    /**
     * Specifies if this gas station instance is a `POIKit.CoFuGasStation`
     */
    var isOnlineCoFuGasStation: Boolean? = null

    /**
     * List of [Amenity]
     */
    var amenities: MutableList<String> = mutableListOf()

    /**
     * List of [Food]s
     */
    var foods: MutableList<String> = mutableListOf()

    /**
     * List of [LoyaltyProgram]s
     */
    var loyaltyPrograms: MutableList<String> = mutableListOf()

    /**
     * List of [PostalService]s
     */
    var postalServices: MutableList<String> = mutableListOf()

    /**
     * List of [Service]s
     */
    var services: MutableList<String> = mutableListOf()

    /**
     * List of [ShopGood]s
     */
    var shopGoods: MutableList<String> = mutableListOf()

    /**
     * Price comparison enabled/disabled
     */
    var priceComparisonOptOut: Boolean? = false

    /**
     * List of CofuPaymentMethods Strings
     */
    var cofuPaymentMethods: MutableList<String> = mutableListOf()

    /**
     * Map of additional properties
     */
    var additionalProperties: Map<String, Any> = mapOf()

    /*protected constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readArrayList(Geometry.CommandGeo::class.java.classLoader) as ArrayList<Geometry.CommandGeo>,
        parcel.readBundle()!!.let {
            it.getSerializable(PARCEL_KEY_VALUES)!! as HashMap<String, String>
        }
    )*/

    /*override fun describeContents(): Int {
        return 0
    }*/

    override fun init(values: HashMap<String, String>) {
        super.init(values)
        this.values = values

        name = values[OSM_POI_NAME]
        brand = values[OSM_BRAND]
        brandId = values[OSM_BRAND_ID]
        currency = values[OSM_CURRENCY]

        values[OSM_VALID_FROM]?.let {
            validFrom = Date(it.toLong().times(1000L)) // UTC UNIX timestamp (seconds) to milliseconds
        }

        values[OSM_PRICE_FORMAT]?.let {
            priceFormat = it
        }

        values[OSM_OPENING_HOURS]?.let { value ->
            OpeningHoursParser.parse(value)?.let { openingHours = it }
        }

        values[OSM_PRICE_LIST]?.let { value ->
            PriceListParser.parse(value).let { prices = it }
        }

        values[OSM_PACE_CONNECTED_FUELING]?.let {
            isConnectedFuelingAvailable = it == "y"
        }

        values[OSM_PAYMENT_METHODS]?.let { value ->
            val methods = value.split(",")
            paymentMethods = methods.toMutableList()
        }

        values[OSM_AMENITIES]?.let { value ->
            val amenities: MutableList<String> = value.split(",").toMutableList()
            this.amenities = amenities
        }

        values[OSM_FOODS]?.let { value ->
            val foods: MutableList<String> = value.split(",").toMutableList()
            this.foods = foods
        }

        values[OSM_LOYALTY_PROGRAMS]?.let { value ->
            val loyaltyPrograms: MutableList<String> = value.split(",").toMutableList()
            this.loyaltyPrograms = loyaltyPrograms
        }

        values[OSM_POSTAL_SERVICES]?.let { value ->
            val postalServices: MutableList<String> = value.split(",").toMutableList()
            this.postalServices = postalServices
        }

        values[OSM_SERVICES]?.let { value ->
            val services: MutableList<String> = value.split(",").toMutableList()
            this.services = services
        }

        values[OSM_SHOP_GOODS]?.let { value ->
            val shopGoods: MutableList<String> = value.split(",").toMutableList()
            this.shopGoods = shopGoods
        }

        values[OSM_PRICE_COMPARISON_OPT_OUT]?.let {
            this.priceComparisonOptOut = it == "y"
        }
    }

    fun isOpen(now: Date) = OpeningHoursUtils.isOpen(now, openingHours)
}

/**
 * An opening hour rule consists of a list of days where the rule applies,
 * a range of hours on the days and a rule stating if the gas station i
 * open or closed during the specified time interval(s).
 */
data class OpeningHours(
    val days: List<Day>,
    val hours: List<OpeningHour>,
    val rule: OpeningRule?
) {
    override fun toString(): String {
        return "$days: $hours: $rule"
    }
}

data class OpeningHour(val from: String, val to: String) {
    override fun toString(): String {
        return "from $from to $to"
    }
}
/**
 * A fuel price entry, consisting of a [FuelType], a display name
 * and a price. The price includes VAT.
 */
data class Price(
    val type: String,
    val name: String?,
    val price: Double?
) {
    override fun toString(): String {
        return "$name: $price"
    }
}

enum class OpeningRule {
    OPEN,
    CLOSED
}

enum class Day {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    companion object {
        fun fromString(d: String): Day? {
            return when (d) {
                "mo" -> MONDAY
                "tu" -> TUESDAY
                "we" -> WEDNESDAY
                "th" -> THURSDAY
                "fr" -> FRIDAY
                "sa" -> SATURDAY
                "su" -> SUNDAY
                else -> null
            }
        }
    }
}
