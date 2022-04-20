//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated.model.response

public data class PaymentData(
    /**
     * Version information about the payment token.
     */
    public val version: String,
    /**
     * Encrypted payment data.
     */
    public val `data`: String,
    /**
     * Signature of the payment and header data.
     */
    public val signature: String,
    /**
     * Additional version-dependent information used to decrypt and verify the payment.
     */
    public val header: Header
)
