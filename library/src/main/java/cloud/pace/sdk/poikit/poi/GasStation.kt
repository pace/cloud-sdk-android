package cloud.pace.sdk.poikit.poi

import androidx.room.Entity
import androidx.room.Ignore
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_AMENITIES
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_BRAND
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
import java.util.*

/**
 * Point of Interest - Gas station with opening hours, prices and payment methods.
 */
@Entity
open class GasStation(id: String, geometry: ArrayList<Geometry.CommandGeo>) :
    PointOfInterest(id, geometry) {

    // TODO: Only for testing. Replace with real price suggestion API data.
    @Ignore
    var priceSuggestion: PriceSuggestion? = PriceSuggestion.UNSET

    @Ignore
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
    var paymentMethods: MutableList<PaymentMethod> = mutableListOf()

    /**
     * Specifies the availability of at least one payment provider for PACE Connected Fueling
     */
    var isConnectedFuelingAvailable: Boolean? = null

    /**
     * Specifies if this gas station instance is a `POIKit.CoFuGasStation`
     */
    var isOnlineCoFuGasStation: Boolean? = null

    /**
     * List of [Amenity]
     */
    var amenities: MutableList<Amenity> = mutableListOf()

    /**
     * List of [Food]s
     */
    var foods: MutableList<Food> = mutableListOf()

    /**
     * List of [LoyaltyProgram]s
     */
    var loyaltyPrograms: MutableList<LoyaltyProgram> = mutableListOf()

    /**
     * List of [PostalService]s
     */
    var postalServices: MutableList<PostalService> = mutableListOf()

    /**
     * List of [Service]s
     */
    var services: MutableList<Service> = mutableListOf()

    /**
     * List of [ShopGood]s
     */
    var shopGoods: MutableList<ShopGood> = mutableListOf()

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
    @Ignore
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
            val splittedResponse = value.split(",")
            val methods: MutableList<PaymentMethod> = mutableListOf()
            splittedResponse.forEach { paymentMethod ->
                PaymentMethod.fromString(paymentMethod)?.let { methods.add(it) }
            }

            cofuPaymentMethods = splittedResponse.filter { it.startsWith("cofu:") }.toMutableList()
            paymentMethods = methods
        }

        values[OSM_AMENITIES]?.let { value ->
            val amenities: MutableList<Amenity> = mutableListOf()
            value.split(",").forEach { amenity ->
                Amenity.fromString(amenity)?.let { amenities.add(it) }
            }
            this.amenities = amenities
        }

        values[OSM_FOODS]?.let { value ->
            val foods: MutableList<Food> = mutableListOf()
            value.split(",").forEach { food ->
                Food.fromString(food)?.let { foods.add(it) }
            }
            this.foods = foods
        }

        values[OSM_LOYALTY_PROGRAMS]?.let { value ->
            val loyaltyPrograms: MutableList<LoyaltyProgram> = mutableListOf()
            value.split(",").forEach { loyaltyProgram ->
                LoyaltyProgram.fromString(loyaltyProgram)?.let { loyaltyPrograms.add(it) }
            }
            this.loyaltyPrograms = loyaltyPrograms
        }

        values[OSM_POSTAL_SERVICES]?.let { value ->
            val postalServices: MutableList<PostalService> = mutableListOf()
            value.split(",").forEach { postalService ->
                PostalService.fromString(postalService)?.let { postalServices.add(it) }
            }
            this.postalServices = postalServices
        }

        values[OSM_SERVICES]?.let { value ->
            val services: MutableList<Service> = mutableListOf()
            value.split(",").forEach { service ->
                Service.fromString(service)?.let { services.add(it) }
            }
            this.services = services
        }

        values[OSM_SHOP_GOODS]?.let { value ->
            val shopGoods: MutableList<ShopGood> = mutableListOf()
            value.split(",").forEach { shopGood ->
                ShopGood.fromString(shopGood)?.let { shopGoods.add(it) }
            }
            this.shopGoods = shopGoods
        }

        values[OSM_PRICE_COMPARISON_OPT_OUT]?.let {
            this.priceComparisonOptOut = it == "y"
        }
    }
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
    val type: FuelType,
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

enum class FuelType(val value: String) {
    E5("ron95e5"),
    E10("ron95e10"),
    DIESEL("diesel"),
    E5_RON98("ron98e5"),
    GAS("lpg"),
    RON98("ron98"),
    E10_RON98("ron98e10"),
    E85("e85"),
    RON91("ron91"),
    RON100("ron100"),
    DIESEL_GTL("dieselGtl"),
    DIESEL_B7("dieselB7"),
    DIESEL_PREMIUM("dieselPremium"),
    COMPRESSED_NATURAL_GAS("cng"),
    LIQUEFIED_NATURAL_GAS("lng"),
    H2("h2"),
    TRUCK_DIESEL("truckDiesel"),
    AD_BLUE("adBlue"),
    UNKNOWN("unknown");

    companion object {
        private val map = FuelType.values().associateBy(FuelType::value)
        fun fromValue(value: String) = map[value]
    }
}

enum class PaymentMethod {
    AMERICAN_EXPRESS,
    APPLE_PAY,
    ALLSTAR_CARD,
    ARAL_KOMFORT,
    AVIA_CARD,
    BARCLAYS,
    BAY_WA_CARD,
    BP_CARD,
    CASH,
    DINERS_CLUB,
    DKV,
    ESSO_CARD,
    ESSO_VOUCHER,
    EUROSHELL,
    FF_CARD,
    GIROCARD,
    GOOGLE_PAY,
    HEM_MYCARD,
    HOYER_CARD,
    JET_CARD,
    LOG_PAY,
    MAESTRO,
    MASTER_CARD,
    NOVOFLEET,
    PACE_PAY,
    PAYPAL,
    ROUTEX,
    SEPA_DIRECT_DEBIT,
    STAR_FLEET_CARD,
    TICKET_CADOU,
    TND_CARD,
    TOTAL_CARD,
    UTA,
    VISA,
    V_PAY,
    WESTFALEN_CARD;

    override fun toString(): String {
        return when (this) {
            AMERICAN_EXPRESS -> "americanExpress"
            APPLE_PAY -> "applePay"
            ALLSTAR_CARD -> "allstarCard"
            ARAL_KOMFORT -> "aralKomfort"
            AVIA_CARD -> "aviaCard"
            BARCLAYS -> "barclays"
            BAY_WA_CARD -> "bayWaCard"
            BP_CARD -> "bpCard"
            CASH -> "cash"
            DINERS_CLUB -> "dinersClub"
            DKV -> "dkv"
            ESSO_CARD -> "essoCard"
            ESSO_VOUCHER -> "essoVoucher"
            EUROSHELL -> "euroshell"
            FF_CARD -> "ffCard"
            GIROCARD -> "girocard"
            GOOGLE_PAY -> "googlePay"
            HEM_MYCARD -> "hemMycard"
            HOYER_CARD -> "hoyercard"
            JET_CARD -> "jetCard"
            LOG_PAY -> "logPay"
            MAESTRO -> "maestro"
            MASTER_CARD -> "masterCard"
            NOVOFLEET -> "novofleet"
            PACE_PAY -> "pacePay"
            PAYPAL -> "paypal"
            ROUTEX -> "routex"
            SEPA_DIRECT_DEBIT -> "sepaDirectDebit"
            STAR_FLEET_CARD -> "starFleetCard"
            TICKET_CADOU -> "ticketCadou"
            TND_CARD -> "tndCard"
            TOTAL_CARD -> "totalCard"
            UTA -> "uta"
            VISA -> "visa"
            V_PAY -> "vPay"
            WESTFALEN_CARD -> "westfalenCard"
        }
    }

    companion object {
        fun fromString(pm: String): PaymentMethod? {
            return when (pm) {
                "americanExpress" -> AMERICAN_EXPRESS
                "applePay" -> APPLE_PAY
                "allstarCard" -> ALLSTAR_CARD
                "aralKomfort" -> ARAL_KOMFORT
                "aviaCard" -> AVIA_CARD
                "barclays" -> BARCLAYS
                "bayWaCard" -> BAY_WA_CARD
                "bpCard" -> BP_CARD
                "cash" -> CASH
                "dinersClub" -> DINERS_CLUB
                "dkv" -> DKV
                "essoCard" -> ESSO_CARD
                "essoVoucher" -> ESSO_VOUCHER
                "euroshell" -> EUROSHELL
                "ffCard" -> FF_CARD
                "girocard" -> GIROCARD
                "googlePay" -> GOOGLE_PAY
                "hemMycard" -> HEM_MYCARD
                "hoyercard" -> HOYER_CARD
                "jetCard" -> JET_CARD
                "logPay" -> LOG_PAY
                "maestro" -> MAESTRO
                "masterCard" -> MASTER_CARD
                "novofleet" -> NOVOFLEET
                "pacePay" -> PACE_PAY
                "paypal" -> PAYPAL
                "routex" -> ROUTEX
                "sepaDirectDebit" -> SEPA_DIRECT_DEBIT
                "starFleetCard" -> STAR_FLEET_CARD
                "ticketCadou" -> TICKET_CADOU
                "tndCard" -> TND_CARD
                "totalCard" -> TOTAL_CARD
                "uta" -> UTA
                "visa" -> VISA
                "vPay" -> V_PAY
                "westfalenCard" -> WESTFALEN_CARD
                else -> null
            }
        }
    }
}

enum class Amenity {
    ATM,
    DISABILITY_FRIENDLY,
    SHOP,
    SHOWER,
    TOILET,
    DIAPER_CHANGING_TABLE,
    TOLL_TERMINAL,
    CAR_PARKING,
    TRUCK_PARKING,
    TRUCK_SUITABLE,
    UNMANNED,
    PAYMENT_TERMINAL,
    MOTEL,
    VENDING_MACHINE;

    override fun toString(): String {
        return when (this) {
            ATM -> "atm"
            DISABILITY_FRIENDLY -> "disabilityFriendly"
            SHOP -> "shop"
            SHOWER -> "shower"
            TOILET -> "toilet"
            DIAPER_CHANGING_TABLE -> "diaperChangingTable"
            TOLL_TERMINAL -> "tollTerminal"
            CAR_PARKING -> "carParking"
            TRUCK_PARKING -> "truckParking"
            TRUCK_SUITABLE -> "truckSuitable"
            UNMANNED -> "unmanned"
            PAYMENT_TERMINAL -> "paymentTerminal"
            MOTEL -> "motel"
            VENDING_MACHINE -> "vendingMachine"
        }
    }

    companion object {
        fun fromString(am: String): Amenity? {
            return when (am) {
                "atm" -> ATM
                "disabilityFriendly" -> DISABILITY_FRIENDLY
                "shop" -> SHOP
                "shower" -> SHOWER
                "toilet" -> TOILET
                "diaperChangingTable" -> DIAPER_CHANGING_TABLE
                "tollTerminal" -> TOLL_TERMINAL
                "carParking" -> CAR_PARKING
                "truckParking" -> TRUCK_PARKING
                "truckSuitable" -> TRUCK_SUITABLE
                "unmanned" -> UNMANNED
                "paymentTerminal" -> PAYMENT_TERMINAL
                "motel" -> MOTEL
                "vendingMachine" -> VENDING_MACHINE
                else -> null
            }
        }
    }
}

enum class Food {
    BAKERY,
    BISTRO,
    CAFE,
    RESTAURANT,
    TAKE_AWAY,
    VEGETARIAN,
    VEGAN;

    override fun toString(): String {
        return when (this) {
            BAKERY -> "bakery"
            BISTRO -> "bistro"
            CAFE -> "cafe"
            RESTAURANT -> "restaurant"
            TAKE_AWAY -> "takeAway"
            VEGETARIAN -> "vegetarian"
            VEGAN -> "vegan"
        }
    }

    companion object {
        fun fromString(fd: String): Food? {
            return when (fd) {
                "bakery" -> BAKERY
                "bistro" -> BISTRO
                "cafe" -> CAFE
                "restaurant" -> RESTAURANT
                "takeAway" -> TAKE_AWAY
                "vegetarian" -> VEGETARIAN
                "vegan" -> VEGAN
                else -> null
            }
        }
    }
}

enum class LoyaltyProgram {
    DEUTSCHLAND_CARD,
    PAYBACK,
    SHELL_CLUBSMART,
    TOTAL_CLUB,
    JOE_KARTE;

    override fun toString(): String {
        return when (this) {
            DEUTSCHLAND_CARD -> "deutschlandCard"
            PAYBACK -> "payback"
            SHELL_CLUBSMART -> "shellClubsmart"
            TOTAL_CLUB -> "totalClub"
            JOE_KARTE -> "joeKarte"
        }
    }

    companion object {
        fun fromString(lp: String): LoyaltyProgram? {
            return when (lp) {
                "deutschlandCard" -> DEUTSCHLAND_CARD
                "payback" -> PAYBACK
                "shellClubsmart" -> SHELL_CLUBSMART
                "totalClub" -> TOTAL_CLUB
                "joeKarte" -> JOE_KARTE
                else -> null
            }
        }
    }
}

enum class PostalService {
    ALZA_BOX,
    DHL,
    DHL_PACKSTATION,
    DPD,
    GEIS_POINT,
    GLS,
    HERMES,
    POST,
    UPS;

    override fun toString(): String {
        return when (this) {
            ALZA_BOX -> "alzaBox"
            DHL -> "dhl"
            DHL_PACKSTATION -> "dhlPackstation"
            DPD -> "dpd"
            GEIS_POINT -> "geisPoint"
            GLS -> "gls"
            HERMES -> "hermes"
            POST -> "post"
            UPS -> "ups"
        }
    }

    companion object {
        fun fromString(ps: String): PostalService? {
            return when (ps) {
                "alzaBox" -> ALZA_BOX
                "dhl" -> DHL
                "dhlPackstation" -> DHL_PACKSTATION
                "dpd" -> DPD
                "geisPoint" -> GEIS_POINT
                "gls" -> GLS
                "hermes" -> HERMES
                "post" -> POST
                "ups" -> UPS
                else -> null
            }
        }
    }
}

enum class Service {
    CAR_WASH,
    FREE_WIFI,
    GAS_BOTTLE_REFILL,
    GAS_STATION_ATTENDANT,
    LAUNDRY_SERVICE,
    LOTTO,
    OIL_SERVICE,

    // PACE_CONNECTED_FUELING,
    SCREEN_WASH_WATER,
    SELF_SERVICE_CAR_WASH,
    TRUCK_WASH,
    TWENTY_FOUR_HOURS_FUELING,
    TWENTY_FOUR_HOURS_SHOPPING,
    TYRE_AIR,
    TYRE_SERVICE,
    VACUUM,
    WIFI,
    WORKSHOP;

    override fun toString(): String {
        return when (this) {
            CAR_WASH -> "carWash"
            FREE_WIFI -> "freeWifi"
            GAS_BOTTLE_REFILL -> "gasBottleRefill"
            GAS_STATION_ATTENDANT -> "gasStationAttendant"
            LAUNDRY_SERVICE -> "laundryService"
            LOTTO -> "lotto"
            OIL_SERVICE -> "oilService"
            // PACE_CONNECTED_FUELING -> "paceConnectedFueling"
            SCREEN_WASH_WATER -> "screenWashWater"
            SELF_SERVICE_CAR_WASH -> "selfServiceCarWash"
            TRUCK_WASH -> "truckWash"
            TWENTY_FOUR_HOURS_FUELING -> "twentyFourHoursFueling"
            TWENTY_FOUR_HOURS_SHOPPING -> "twentyFourHoursShopping"
            TYRE_AIR -> "tyreAir"
            TYRE_SERVICE -> "tyreService"
            VACUUM -> "vacuum"
            WIFI -> "wifi"
            WORKSHOP -> "workshop"
        }
    }

    companion object {
        fun fromString(sv: String): Service? {
            return when (sv) {
                "carWash" -> CAR_WASH
                "freeWifi" -> FREE_WIFI
                "gasBottleRefill" -> GAS_BOTTLE_REFILL
                "gasStationAttendant" -> GAS_STATION_ATTENDANT
                "laundryService" -> LAUNDRY_SERVICE
                "lotto" -> LOTTO
                "oilService" -> OIL_SERVICE
                // "paceConnectedFueling" -> PACE_CONNECTED_FUELING
                "screenWashWater" -> SCREEN_WASH_WATER
                "selfServiceCarWash" -> SELF_SERVICE_CAR_WASH
                "truckWash" -> TRUCK_WASH
                "twentyFourHoursFueling" -> TWENTY_FOUR_HOURS_FUELING
                "twentyFourHoursShopping" -> TWENTY_FOUR_HOURS_SHOPPING
                "tyreAir" -> TYRE_AIR
                "tyreService" -> TYRE_SERVICE
                "vacuum" -> VACUUM
                "wifi" -> WIFI
                "workshop" -> WORKSHOP
                else -> null
            }
        }
    }
}

enum class ShopGood {
    AD_BLUE,
    CONTACT_LENSES,
    CRUSHED_ICE,
    FLOWERS,
    LUBRICANTS,
    VIGNETTES;

    override fun toString(): String {
        return when (this) {
            AD_BLUE -> "adBlue"
            CONTACT_LENSES -> "contactLenses"
            CRUSHED_ICE -> "crushedIce"
            FLOWERS -> "flowers"
            LUBRICANTS -> "lubricants"
            VIGNETTES -> "vignettes"
        }
    }

    companion object {
        fun fromString(sg: String): ShopGood? {
            return when (sg) {
                "adBlue" -> AD_BLUE
                "contactLenses" -> CONTACT_LENSES
                "crushedIce" -> CRUSHED_ICE
                "flowers" -> FLOWERS
                "lubricants" -> LUBRICANTS
                "vignettes" -> VIGNETTES
                else -> null
            }
        }
    }
}

enum class PriceSuggestion {
    LOW,
    MEDIUM,
    HIGH,
    UNSET,
    NO_DATA,
    CLOSED
}
