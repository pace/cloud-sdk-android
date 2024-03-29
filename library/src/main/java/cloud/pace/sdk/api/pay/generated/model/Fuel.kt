/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.model

class Fuel {

    /* Fuel amount in provided unit */
    var amount: Double? = null

    /* Price per unit (with three decimal places) */
    var pricePerUnit: Double? = null

    /* Product Name of the current fuel.productName */
    var productName: String? = null

    /* Number of the pump used for fueling, i.e., the actual number that is being displayed to the customer */
    var pumpNumber: Int? = null

    /* Fuel measurement unit. Eg: `liter`, `us-gallon`, `uk-gallon`, `kilogram` */
    var unit: String? = null
}
