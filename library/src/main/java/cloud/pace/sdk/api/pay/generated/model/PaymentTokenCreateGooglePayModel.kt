/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.model

import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource

@JsonApi(type = "paymentToken")
class PaymentTokenCreateGooglePay : Resource() {

    /* Currency as specified in ISO-4217. */
    lateinit var currency: String
    var amount: Double? = null

    /* PACE resource name(s) of one or multiple resources, for which the payment should be authorized. */
    lateinit var purposePRNs: List<String>

    /* The encrypted data received from GooglePay */
    lateinit var googlePay: GooglePay
    var discountTokens: List<String>? = null

    /* The code and method for two factor authentication, if required by the payment method */
    var twoFactor: TwoFactor? = null

    /* The encrypted data received from GooglePay */
    class GooglePay {

        var paymentData: PaymentData? = null
        var signature: String? = null
        var version: String? = null

        /* The encrypted data received from GooglePay */
        class PaymentData {

            var encryptedMessage: String? = null
            var ephemeralPublicKey: String? = null
            var tag: String? = null
        }
    }

    /* The code and method for two factor authentication, if required by the payment method */
    class TwoFactor {

        /* A single name for the 2fa e.g. `face-id`, `fingerprint`, `biometry`, `password`, `pin` */
        var method: String? = null

        /* OTP (One time password) for the authorization. */
        var otp: String? = null
    }
}
