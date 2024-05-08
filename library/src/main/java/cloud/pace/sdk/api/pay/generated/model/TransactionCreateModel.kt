/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.model

import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource

@JsonApi(type = "transaction")
class TransactionCreate : Resource() {

    /* Payment token value */
    lateinit var paymentToken: String

    /* PACE resource name - referring to the transaction purpose */
    lateinit var purposePRN: String

    /* PACE resource name - referring to the transaction purpose with provider details */
    lateinit var providerPRN: String
    var vat: VAT? = null

    /* additional data for omv */
    var additionalData: String? = null

    /* Currency as specified in ISO-4217. */
    var currency: String? = null

    /* Driver/vehicle identification */
    var driverVehicleID: String? = null
    var fuel: Fuel? = null

    /* Fuel amount */
    var fuelAmount: Double? = null

    /* Product name */
    var fuelProductName: String? = null

    /* PACE resource name - referring to the transaction issuer */
    var issuerPRN: String? = null

    /* PACE resource name - referring to the transaction's merchant */
    var merchantPRN: String? = null
    var metadata: List<TransactionMetadata>? = null

    /* Current mileage in meters */
    var mileage: Int? = null

    /* Number plate of the car */
    var numberPlate: String? = null
    var priceExcludingVAT: Double? = null
    var priceIncludingVAT: Double? = null

    /* The given productFlow (e.g. preAuth, postPay) */
    var productFlow: String? = null

    /* Set to true if the payment is for an unattended process */
    var unattended: Boolean? = null

    /* Vehicle identification number */
    var vin: String? = null

    class VAT {

        var amount: Double? = null

        /* *Important:* Vat rates have to be between 0.00 and 1.00 and not have a decimal precision beyoned 2, i.e., no rate of 0.119999999
     */
        var rate: Double? = null
    }
}