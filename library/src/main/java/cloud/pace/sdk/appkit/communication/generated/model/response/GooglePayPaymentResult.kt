//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated.model.response

public data class GooglePayPaymentResponse(
    public val apiVersion: Int,
    public val apiVersionMinor: Int,
    public val paymentMethodData: PaymentMethodData,
    public val email: String?,
    public val shippingAddress: ShippingAddress?,
) : ResponseBody()

public data class GooglePayPaymentError(
    public val message: String? = null,
) : ResponseBody()

public class GooglePayPaymentResult private constructor(
    status: Int,
    body: ResponseBody?,
) : Result(status, body) {
    public constructor(success: Success) : this(200, success.response)

    public constructor(failure: Failure) : this(failure.statusCode.code, failure.response)

    public class Success(
        public val response: GooglePayPaymentResponse,
    )

    public class Failure(
        public val statusCode: StatusCode,
        public val response: GooglePayPaymentError,
    ) {
        public enum class StatusCode(
            public val code: Int,
        ) {
            BadRequest(400),
            RequestTimeout(408),
            ClientClosedRequest(499),
            InternalServerError(500),
            ;
        }
    }
}
