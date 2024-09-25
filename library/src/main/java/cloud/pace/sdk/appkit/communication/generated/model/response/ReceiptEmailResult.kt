//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated.model.response

public data class ReceiptEmailResponse(
    public val email: String?
) : ResponseBody()

public data class ReceiptEmailError(
    public val message: String? = null
) : ResponseBody()

public class ReceiptEmailResult private constructor(
    status: Int,
    body: ResponseBody?
) : Result(status, body) {
    public constructor(success: Success) : this(200, success.response)

    public constructor(failure: Failure) : this(failure.statusCode.code, failure.response)

    public class Success(
        public val response: ReceiptEmailResponse
    )

    public class Failure(
        public val statusCode: StatusCode,
        public val response: ReceiptEmailError
    ) {
        public enum class StatusCode(
            public val code: Int
        ) {
            BadRequest(400),
            RequestTimeout(408),
            InternalServerError(500),
            ;
        }
    }
}