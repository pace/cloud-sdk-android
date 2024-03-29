/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.fueling.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource

@JsonApi(type = "transactionRequest")
class TransactionRequest : Resource() {

    /* 'Value' field of the payment token (not the payment token ID) */
    lateinit var paymentToken: String

    /* Pump ID */
    lateinit var pumpId: String

    /* A callback URL to report the status of the transaction to, once completed. Only relevant if unattendedPayment is true */
    var callbackURL: String? = null

    /* Fuel type for cars, based on the EU fuel marking */
    var carFuelType: CarFuelType? = null

    /* Currency as specified in ISO-4217. */
    var currency: String? = null

    /* Current mileage in meters */
    var mileage: Int? = null

    /* Number plate of the Vehicle */
    var numberPlate: String? = null
    var priceIncludingVAT: Double? = null

    /* Set to 'true' if you want the payment to be cleared automatically in the background after fueling */
    var unattendedPayment: Boolean? = null

    /* Vehicle identification number */
    var vin: String? = null

    /* Fuel type for cars, based on the EU fuel marking */
    enum class CarFuelType(val value: String) {
        @SerializedName("ron98")
        @Json(name = "ron98")
        RON98("ron98"),

        @SerializedName("ron98e5")
        @Json(name = "ron98e5")
        RON98E5("ron98e5"),

        @SerializedName("ron95e10")
        @Json(name = "ron95e10")
        RON95E10("ron95e10"),

        @SerializedName("diesel")
        @Json(name = "diesel")
        DIESEL("diesel"),

        @SerializedName("e85")
        @Json(name = "e85")
        E85("e85"),

        @SerializedName("ron91")
        @Json(name = "ron91")
        RON91("ron91"),

        @SerializedName("ron95e5")
        @Json(name = "ron95e5")
        RON95E5("ron95e5"),

        @SerializedName("ron100")
        @Json(name = "ron100")
        RON100("ron100"),

        @SerializedName("dieselGtl")
        @Json(name = "dieselGtl")
        DIESELGTL("dieselGtl"),

        @SerializedName("dieselB7")
        @Json(name = "dieselB7")
        DIESELB7("dieselB7"),

        @SerializedName("dieselB15")
        @Json(name = "dieselB15")
        DIESELB15("dieselB15"),

        @SerializedName("dieselPremium")
        @Json(name = "dieselPremium")
        DIESELPREMIUM("dieselPremium"),

        @SerializedName("lpg")
        @Json(name = "lpg")
        LPG("lpg"),

        @SerializedName("cng")
        @Json(name = "cng")
        CNG("cng"),

        @SerializedName("lng")
        @Json(name = "lng")
        LNG("lng"),

        @SerializedName("h2")
        @Json(name = "h2")
        H2("h2"),

        @SerializedName("truckDiesel")
        @Json(name = "truckDiesel")
        TRUCKDIESEL("truckDiesel"),

        @SerializedName("adBlue")
        @Json(name = "adBlue")
        ADBLUE("adBlue"),

        @SerializedName("truckAdBlue")
        @Json(name = "truckAdBlue")
        TRUCKADBLUE("truckAdBlue"),

        @SerializedName("truckDieselPremium")
        @Json(name = "truckDieselPremium")
        TRUCKDIESELPREMIUM("truckDieselPremium"),

        @SerializedName("truckLpg")
        @Json(name = "truckLpg")
        TRUCKLPG("truckLpg"),

        @SerializedName("heatingOil")
        @Json(name = "heatingOil")
        HEATINGOIL("heatingOil")
    }
}
