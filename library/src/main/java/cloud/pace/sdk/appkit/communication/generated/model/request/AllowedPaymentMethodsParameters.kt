//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated.model.request

import kotlin.Boolean
import kotlin.String
import kotlin.collections.List

public data class AllowedPaymentMethodsParameters(
    /**
     * Fields supported to authenticate a card transaction.
     */
    public val allowedAuthMethods: List<String>,
    /**
     * One or more card networks that you support, also supported by the Google Pay API, e.g.
     * MASTERCARD, VISA
     */
    public val allowedCardNetworks: List<String>,
    /**
     * Set to false if you don't support prepaid cards. The prepaid card class is supported for the
     * card networks specified by default.
     */
    public val allowPrepaidCards: Boolean?,
    /**
     * Set to false if you don't support credit cards. The credit card class is supported for the card
     * networks specified by default.
     */
    public val allowCreditCards: Boolean?,
    /**
     * Set to true to request assuranceDetails. This object provides information about the validation
     * performed on the returned payment data.
     */
    public val assuranceDetailsRequired: Boolean?,
    /**
     * Set to true if you require a billing address. A billing address should only be requested if
     * it's required to process the transaction. Additional data requests can increase friction in the
     * checkout process and lead to a lower conversion rate.
     */
    public val billingAddressRequired: Boolean?,
    /**
     * This object allows you to set additional fields to be returned for a requested billing address.
     */
    public val billingAddressParameters: BillingAddressParameters?
)
