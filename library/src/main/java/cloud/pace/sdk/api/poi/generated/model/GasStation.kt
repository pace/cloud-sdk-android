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

@JsonApi(type = "gasStation")
class GasStation : Resource() {

    var address: Address? = null
    var amenities: List<Amenities>? = null
    var brand: String? = null
    var contact: Contact? = null
    var food: List<Food>? = null
    var latitude: Float? = null
    var longitude: Float? = null
    var loyaltyPrograms: List<LoyaltyPrograms>? = null
    var openingHours: CommonOpeningHours? = null
    var paymentMethods: List<PaymentMethods>? = null
    var postalServices: List<PostalServices>? = null
    var priceFormat: String? = null
    /* References are PRNs to external and internal resources that are represented by this poi */
    var references: List<String>? = null
    var services: List<Services>? = null
    var shopGoods: List<ShopGoods>? = null
    var stationName: String? = null

    enum class Amenities(val value: String) {
        @SerializedName("atm")
        @Json(name = "atm")
        ATM("atm"),
        @SerializedName("disabilityFriendly")
        @Json(name = "disabilityFriendly")
        DISABILITYFRIENDLY("disabilityFriendly"),
        @SerializedName("shop")
        @Json(name = "shop")
        SHOP("shop"),
        @SerializedName("shower")
        @Json(name = "shower")
        SHOWER("shower"),
        @SerializedName("toilet")
        @Json(name = "toilet")
        TOILET("toilet"),
        @SerializedName("tollTerminal")
        @Json(name = "tollTerminal")
        TOLLTERMINAL("tollTerminal"),
        @SerializedName("carParking")
        @Json(name = "carParking")
        CARPARKING("carParking"),
        @SerializedName("truckParking")
        @Json(name = "truckParking")
        TRUCKPARKING("truckParking"),
        @SerializedName("truckSuitable")
        @Json(name = "truckSuitable")
        TRUCKSUITABLE("truckSuitable"),
        @SerializedName("unmanned")
        @Json(name = "unmanned")
        UNMANNED("unmanned"),
        @SerializedName("paymentTerminal")
        @Json(name = "paymentTerminal")
        PAYMENTTERMINAL("paymentTerminal"),
        @SerializedName("motel")
        @Json(name = "motel")
        MOTEL("motel"),
        @SerializedName("carSuitable")
        @Json(name = "carSuitable")
        CARSUITABLE("carSuitable")
    }

    enum class Food(val value: String) {
        @SerializedName("bakery")
        @Json(name = "bakery")
        BAKERY("bakery"),
        @SerializedName("bistro")
        @Json(name = "bistro")
        BISTRO("bistro"),
        @SerializedName("cafe")
        @Json(name = "cafe")
        CAFE("cafe"),
        @SerializedName("restaurant")
        @Json(name = "restaurant")
        RESTAURANT("restaurant"),
        @SerializedName("takeaway")
        @Json(name = "takeaway")
        TAKEAWAY("takeaway")
    }

    enum class LoyaltyPrograms(val value: String) {
        @SerializedName("deutschlandCard")
        @Json(name = "deutschlandCard")
        DEUTSCHLANDCARD("deutschlandCard"),
        @SerializedName("payback")
        @Json(name = "payback")
        PAYBACK("payback"),
        @SerializedName("shellClubsmart")
        @Json(name = "shellClubsmart")
        SHELLCLUBSMART("shellClubsmart"),
        @SerializedName("totalClub")
        @Json(name = "totalClub")
        TOTALCLUB("totalClub")
    }

    enum class PaymentMethods(val value: String) {
        @SerializedName("americanExpress")
        @Json(name = "americanExpress")
        AMERICANEXPRESS("americanExpress"),
        @SerializedName("applyPay")
        @Json(name = "applyPay")
        APPLYPAY("applyPay"),
        @SerializedName("aralKomfort")
        @Json(name = "aralKomfort")
        ARALKOMFORT("aralKomfort"),
        @SerializedName("aviaCard")
        @Json(name = "aviaCard")
        AVIACARD("aviaCard"),
        @SerializedName("barclays")
        @Json(name = "barclays")
        BARCLAYS("barclays"),
        @SerializedName("bayWaCard")
        @Json(name = "bayWaCard")
        BAYWACARD("bayWaCard"),
        @SerializedName("cash")
        @Json(name = "cash")
        CASH("cash"),
        @SerializedName("dinersClub")
        @Json(name = "dinersClub")
        DINERSCLUB("dinersClub"),
        @SerializedName("dkv")
        @Json(name = "dkv")
        DKV("dkv"),
        @SerializedName("essoCard")
        @Json(name = "essoCard")
        ESSOCARD("essoCard"),
        @SerializedName("essoVoucher")
        @Json(name = "essoVoucher")
        ESSOVOUCHER("essoVoucher"),
        @SerializedName("euroshell")
        @Json(name = "euroshell")
        EUROSHELL("euroshell"),
        @SerializedName("ffCard")
        @Json(name = "ffCard")
        FFCARD("ffCard"),
        @SerializedName("girocard")
        @Json(name = "girocard")
        GIROCARD("girocard"),
        @SerializedName("googlePay")
        @Json(name = "googlePay")
        GOOGLEPAY("googlePay"),
        @SerializedName("hemMycard")
        @Json(name = "hemMycard")
        HEMMYCARD("hemMycard"),
        @SerializedName("jetCard")
        @Json(name = "jetCard")
        JETCARD("jetCard"),
        @SerializedName("logPay")
        @Json(name = "logPay")
        LOGPAY("logPay"),
        @SerializedName("maestro")
        @Json(name = "maestro")
        MAESTRO("maestro"),
        @SerializedName("masterCard")
        @Json(name = "masterCard")
        MASTERCARD("masterCard"),
        @SerializedName("novofleet")
        @Json(name = "novofleet")
        NOVOFLEET("novofleet"),
        @SerializedName("pacePay")
        @Json(name = "pacePay")
        PACEPAY("pacePay"),
        @SerializedName("paypal")
        @Json(name = "paypal")
        PAYPAL("paypal"),
        @SerializedName("routex")
        @Json(name = "routex")
        ROUTEX("routex"),
        @SerializedName("sepaDirectDebit")
        @Json(name = "sepaDirectDebit")
        SEPADIRECTDEBIT("sepaDirectDebit"),
        @SerializedName("starFleetCard")
        @Json(name = "starFleetCard")
        STARFLEETCARD("starFleetCard"),
        @SerializedName("tndCard")
        @Json(name = "tndCard")
        TNDCARD("tndCard"),
        @SerializedName("totalCard")
        @Json(name = "totalCard")
        TOTALCARD("totalCard"),
        @SerializedName("uta")
        @Json(name = "uta")
        UTA("uta"),
        @SerializedName("visa")
        @Json(name = "visa")
        VISA("visa"),
        @SerializedName("vPay")
        @Json(name = "vPay")
        VPAY("vPay"),
        @SerializedName("westfalenCard")
        @Json(name = "westfalenCard")
        WESTFALENCARD("westfalenCard")
    }

    enum class PostalServices(val value: String) {
        @SerializedName("dhl")
        @Json(name = "dhl")
        DHL("dhl"),
        @SerializedName("dhlPackstation")
        @Json(name = "dhlPackstation")
        DHLPACKSTATION("dhlPackstation"),
        @SerializedName("dpd")
        @Json(name = "dpd")
        DPD("dpd"),
        @SerializedName("gls")
        @Json(name = "gls")
        GLS("gls"),
        @SerializedName("hermes")
        @Json(name = "hermes")
        HERMES("hermes"),
        @SerializedName("post")
        @Json(name = "post")
        POST("post"),
        @SerializedName("ups")
        @Json(name = "ups")
        UPS("ups")
    }

    enum class Services(val value: String) {
        @SerializedName("carWash")
        @Json(name = "carWash")
        CARWASH("carWash"),
        @SerializedName("freeWifi")
        @Json(name = "freeWifi")
        FREEWIFI("freeWifi"),
        @SerializedName("gasBottleRefill")
        @Json(name = "gasBottleRefill")
        GASBOTTLEREFILL("gasBottleRefill"),
        @SerializedName("gasStationAttendant")
        @Json(name = "gasStationAttendant")
        GASSTATIONATTENDANT("gasStationAttendant"),
        @SerializedName("laundryService")
        @Json(name = "laundryService")
        LAUNDRYSERVICE("laundryService"),
        @SerializedName("lotto")
        @Json(name = "lotto")
        LOTTO("lotto"),
        @SerializedName("oilService")
        @Json(name = "oilService")
        OILSERVICE("oilService"),
        @SerializedName("paceConnectedFueling")
        @Json(name = "paceConnectedFueling")
        PACECONNECTEDFUELING("paceConnectedFueling"),
        @SerializedName("screenWashWater")
        @Json(name = "screenWashWater")
        SCREENWASHWATER("screenWashWater"),
        @SerializedName("selfServiceCarWash")
        @Json(name = "selfServiceCarWash")
        SELFSERVICECARWASH("selfServiceCarWash"),
        @SerializedName("truckWash")
        @Json(name = "truckWash")
        TRUCKWASH("truckWash"),
        @SerializedName("twentyFourHoursFueling")
        @Json(name = "twentyFourHoursFueling")
        TWENTYFOURHOURSFUELING("twentyFourHoursFueling"),
        @SerializedName("twentyFourHoursShopping")
        @Json(name = "twentyFourHoursShopping")
        TWENTYFOURHOURSSHOPPING("twentyFourHoursShopping"),
        @SerializedName("tyreAir")
        @Json(name = "tyreAir")
        TYREAIR("tyreAir"),
        @SerializedName("tyreService")
        @Json(name = "tyreService")
        TYRESERVICE("tyreService"),
        @SerializedName("vacuum")
        @Json(name = "vacuum")
        VACUUM("vacuum"),
        @SerializedName("wifi")
        @Json(name = "wifi")
        WIFI("wifi"),
        @SerializedName("workshop")
        @Json(name = "workshop")
        WORKSHOP("workshop")
    }

    enum class ShopGoods(val value: String) {
        @SerializedName("adBlue")
        @Json(name = "adBlue")
        ADBLUE("adBlue"),
        @SerializedName("contactLenses")
        @Json(name = "contactLenses")
        CONTACTLENSES("contactLenses"),
        @SerializedName("crushedIce")
        @Json(name = "crushedIce")
        CRUSHEDICE("crushedIce"),
        @SerializedName("flowers")
        @Json(name = "flowers")
        FLOWERS("flowers"),
        @SerializedName("vignette")
        @Json(name = "vignette")
        VIGNETTE("vignette"),
        @SerializedName("lubricants")
        @Json(name = "lubricants")
        LUBRICANTS("lubricants")
    }

    class Address {

        var city: String? = null
        /* Country code in as specified in ISO 3166-1. */
        var countryCode: String? = null
        var houseNo: String? = null
        var postalCode: String? = null
        var street: String? = null
    }

    class Contact {

        var email: String? = null
        var faxNumber: String? = null
        var firstName: String? = null
        var gender: Gender? = null
        var lastName: String? = null
        var phoneNumber: String? = null

        enum class Gender(val value: String) {
            @SerializedName("m")
            @Json(name = "m")
            M("m"),
            @SerializedName("f")
            @Json(name = "f")
            F("f"),
            @SerializedName("o")
            @Json(name = "o")
            O("o")
        }
    }

    private var fuelPrices: HasMany<FuelPrice> = HasMany()
    fun getFuelPrices() = fuelPrices.get(document)

    private var locationBasedApps: HasMany<LocationBasedApp> = HasMany()
    fun getLocationBasedApps() = locationBasedApps.get(document)

    private var referenceStatuses: HasMany<ReferenceStatus> = HasMany()
    fun getReferenceStatuses() = referenceStatuses.get(document)

    private var sucessorOf: HasMany<GasStation> = HasMany()
    fun getSucessorOf() = sucessorOf.get(document)


}
