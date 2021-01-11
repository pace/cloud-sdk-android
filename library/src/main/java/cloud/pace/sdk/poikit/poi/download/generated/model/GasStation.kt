/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.model

import moe.banana.jsonapi2.HasMany
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import com.squareup.moshi.Json
import java.util.*

@JsonApi(type = "gasStation")
class GasStation : Resource() {
   var address: Address? = null
   var amenities: List<String>? = null
   var brand: String? = null
   var contact: Contact? = null
   var food: List<String>? = null
   var latitude: Float? = null
   var longitude: Float? = null
   var loyaltyPrograms: List<String>? = null
   var openingHours: CommonOpeningHours? = null
   var paymentMethods: List<String>? = null
   var postalServices: List<String>? = null
   var priceFormat: String? = null
/** References are PRNs to external and internal resources that are represented by this poi **/
   var references: List<String>? = null
   var services: List<String>? = null
   var shopGoods: List<String>? = null
   var stationName: String? = null

    enum class Amenities(val value: String) {
        ATM("atm"),
        DISABILITYFRIENDLY("disabilityFriendly"),
        SHOP("shop"),
        SHOWER("shower"),
        TOILET("toilet"),
        TOLLTERMINAL("tollTerminal"),
        CARPARKING("carParking"),
        TRUCKPARKING("truckParking"),
        TRUCKSUITABLE("truckSuitable"),
        UNMANNED("unmanned"),
        PAYMENTTERMINAL("paymentTerminal"),
        MOTEL("motel"),
        CARSUITABLE("carSuitable")
    }

    enum class Food(val value: String) {
        BAKERY("bakery"),
        BISTRO("bistro"),
        CAFE("cafe"),
        RESTAURANT("restaurant"),
        TAKEAWAY("takeaway")
    }

    enum class LoyaltyPrograms(val value: String) {
        DEUTSCHLANDCARD("deutschlandCard"),
        PAYBACK("payback"),
        SHELLCLUBSMART("shellClubsmart"),
        TOTALCLUB("totalClub")
    }

    enum class PaymentMethods(val value: String) {
        AMERICANEXPRESS("americanExpress"),
        APPLYPAY("applyPay"),
        ARALKOMFORT("aralKomfort"),
        AVIACARD("aviaCard"),
        BARCLAYS("barclays"),
        BAYWACARD("bayWaCard"),
        CASH("cash"),
        DINERSCLUB("dinersClub"),
        DKV("dkv"),
        ESSOCARD("essoCard"),
        ESSOVOUCHER("essoVoucher"),
        EUROSHELL("euroshell"),
        FFCARD("ffCard"),
        GIROCARD("girocard"),
        GOOGLEPAY("googlePay"),
        HEMMYCARD("hemMycard"),
        JETCARD("jetCard"),
        LOGPAY("logPay"),
        MAESTRO("maestro"),
        MASTERCARD("masterCard"),
        NOVOFLEET("novofleet"),
        PACEPAY("pacePay"),
        PAYPAL("paypal"),
        ROUTEX("routex"),
        SEPADIRECTDEBIT("sepaDirectDebit"),
        STARFLEETCARD("starFleetCard"),
        TNDCARD("tndCard"),
        TOTALCARD("totalCard"),
        UTA("uta"),
        VISA("visa"),
        VPAY("vPay"),
        WESTFALENCARD("westfalenCard")
    }

    enum class PostalServices(val value: String) {
        DHL("dhl"),
        DHLPACKSTATION("dhlPackstation"),
        DPD("dpd"),
        GLS("gls"),
        HERMES("hermes"),
        POST("post"),
        UPS("ups")
    }

    enum class Services(val value: String) {
        CARWASH("carWash"),
        FREEWIFI("freeWifi"),
        GASBOTTLEREFILL("gasBottleRefill"),
        GASSTATIONATTENDANT("gasStationAttendant"),
        LAUNDRYSERVICE("laundryService"),
        LOTTO("lotto"),
        OILSERVICE("oilService"),
        PACECONNECTEDFUELING("paceConnectedFueling"),
        SCREENWASHWATER("screenWashWater"),
        SELFSERVICECARWASH("selfServiceCarWash"),
        TRUCKWASH("truckWash"),
        TWENTYFOURHOURSFUELING("twentyFourHoursFueling"),
        TWENTYFOURHOURSSHOPPING("twentyFourHoursShopping"),
        TYREAIR("tyreAir"),
        TYRESERVICE("tyreService"),
        VACUUM("vacuum"),
        WIFI("wifi"),
        WORKSHOP("workshop")
    }

    enum class ShopGoods(val value: String) {
        ADBLUE("adBlue"),
        CONTACTLENSES("contactLenses"),
        CRUSHEDICE("crushedIce"),
        FLOWERS("flowers"),
        VIGNETTE("vignette"),
        LUBRICANTS("lubricants")
    }

    class Address{
       var city: String? = null
    /** Country code in as specified in ISO 3166-1. **/
       var countryCode: String? = null
       var houseNo: String? = null
       var postalCode: String? = null
       var street: String? = null
    }

    class Contact{
       var email: String? = null
       var faxNumber: String? = null
       var firstName: String? = null
       var gender: String? = null
       var lastName: String? = null
       var phoneNumber: String? = null

        enum class Gender(val value: String) {
            M("m"),
            F("f"),
            O("o")
        }
    }
   lateinit var fuelPrices: HasMany<FuelPrice>
   lateinit var locationBasedApps: HasMany<LocationBasedApp>
   lateinit var referenceStatuses: HasMany<ReferenceStatus>
   lateinit var sucessorOf: HasMany<GasStation>

   fun getFuelPrices() = fuelPrices.get(document)

   fun getLocationBasedApps() = locationBasedApps.get(document)

   fun getReferenceStatuses() = referenceStatuses.get(document)

   fun getSucessorOf() = sucessorOf.get(document)
}