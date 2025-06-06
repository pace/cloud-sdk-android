//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated.model.response

public data class IsSignedInResponse(
    public val signedIn: Boolean,
) : ResponseBody()

public data class IsSignedInError(
    public val message: String? = null,
) : ResponseBody()

public class IsSignedInResult private constructor(
    status: Int,
    body: ResponseBody?,
) : Result(status, body) {
    public constructor(success: Success) : this(200, success.response)

    public constructor(failure: Failure) : this(failure.statusCode.code, failure.response)

    public class Success(
        public val response: IsSignedInResponse,
    )

    public class Failure(
        public val statusCode: StatusCode,
        public val response: IsSignedInError,
    ) {
        public enum class StatusCode(
            public val code: Int,
        ) {
            BadRequest(400),
            RequestTimeout(408),
            InternalServerError(500),
            ;
        }
    }
}
