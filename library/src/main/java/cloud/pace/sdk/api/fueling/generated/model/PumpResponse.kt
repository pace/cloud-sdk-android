/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.fueling.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import moe.banana.jsonapi2.HasMany
import moe.banana.jsonapi2.HasOne
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.*

@JsonApi(type = "pumpResponse")
class PumpResponse : Resource() {

    var vat: VAT? = null
    /* Currency as specified in ISO-4217. */
    var currency: String? = null
    /* Fuel amount in liters */
    var fuelAmount: Double? = null
    var fuelType: String? = null
    /* The fueling process that has to be followed
* `postPay` the pump is *free* and needs to be [paid](#operation/ProcessPayment) after fueling
* `preAuth` the pump is *locked* and has to be [unlocked](#operation/ProcessPayment)
* `preAuthWithFuelType` the pump is *locked* and has to be [unlocked](#operation/ProcessPayment), the `carFuelType` is required
 */
    var fuelingProcess: FuelingProcess? = null
    /* Pump identifier */
    var identifier: String? = null
    var priceIncludingVAT: Double? = null
    /* Fuel price in CUR/liter */
    var pricePerUnit: Double? = null
    var priceWithoutVAT: Double? = null
    var productName: String? = null
    /* Current pump status.
* `free` the pump is free, fueling possible (nozzle not lifted), possible transitions *inUse*, *locked*, *outOfOrder*. Note: A transition from *free* to *locked* may implies the pump was pre-authorization was canceled.
* `inUse` the pump is fueling, possible transitions *readyToPay*, *locked*, *outOfOrder*
* `readyToPay` the pump can be payed using the post pay process, possible transitions *free*, *locked*, *outOfOrder*. Note: A transition from *readyToPay* to *free* implies the pump was paid.
* `locked` the pump required a pre-authorization, possible transitions *free*, *inTransaction*, *outOfOrder*. Note: A transition from *locked* to *free* implies the pre-authorization was successful.
* `inTransaction` the pump is in use by another user using the pre-authorization process, possible transitions *locked*, *outOfOrder*
* `outOfOrder` the pump has a technical problem, this can only be resolved by the gas station staff on site, possible transitions *free*, *locked*. Note: The customer has to pay in the shop
 */
    var status: Status? = null
    var transaction: Transaction? = null
    /* Provided if the user pre-authorized the pump */
    var transactionId: String? = null

    /* The fueling process that has to be followed
    * `postPay` the pump is *free* and needs to be [paid](#operation/ProcessPayment) after fueling
    * `preAuth` the pump is *locked* and has to be [unlocked](#operation/ProcessPayment)
    * `preAuthWithFuelType` the pump is *locked* and has to be [unlocked](#operation/ProcessPayment), the `carFuelType` is required
     */
    enum class FuelingProcess(val value: String) {
        @SerializedName("postPay")
        @Json(name = "postPay")
        POSTPAY("postPay"),
        @SerializedName("preAuth")
        @Json(name = "preAuth")
        PREAUTH("preAuth"),
        @SerializedName("preAuthWithFuelType")
        @Json(name = "preAuthWithFuelType")
        PREAUTHWITHFUELTYPE("preAuthWithFuelType")
    }

    /* Current pump status.
    * `free` the pump is free, fueling possible (nozzle not lifted), possible transitions *inUse*, *locked*, *outOfOrder*. Note: A transition from *free* to *locked* may implies the pump was pre-authorization was canceled.
    * `inUse` the pump is fueling, possible transitions *readyToPay*, *locked*, *outOfOrder*
    * `readyToPay` the pump can be payed using the post pay process, possible transitions *free*, *locked*, *outOfOrder*. Note: A transition from *readyToPay* to *free* implies the pump was paid.
    * `locked` the pump required a pre-authorization, possible transitions *free*, *inTransaction*, *outOfOrder*. Note: A transition from *locked* to *free* implies the pre-authorization was successful.
    * `inTransaction` the pump is in use by another user using the pre-authorization process, possible transitions *locked*, *outOfOrder*
    * `outOfOrder` the pump has a technical problem, this can only be resolved by the gas station staff on site, possible transitions *free*, *locked*. Note: The customer has to pay in the shop
     */
    enum class Status(val value: String) {
        @SerializedName("free")
        @Json(name = "free")
        FREE("free"),
        @SerializedName("inUse")
        @Json(name = "inUse")
        INUSE("inUse"),
        @SerializedName("readyToPay")
        @Json(name = "readyToPay")
        READYTOPAY("readyToPay"),
        @SerializedName("locked")
        @Json(name = "locked")
        LOCKED("locked"),
        @SerializedName("inTransaction")
        @Json(name = "inTransaction")
        INTRANSACTION("inTransaction"),
        @SerializedName("outOfOrder")
        @Json(name = "outOfOrder")
        OUTOFORDER("outOfOrder")
    }

    class VAT {

        var amount: Double? = null
        var rate: Double? = null
    }

}
