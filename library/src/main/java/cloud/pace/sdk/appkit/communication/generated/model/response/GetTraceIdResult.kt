//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated.model.response

public data class GetTraceIdResponse(
    public val `value`: String,
) : ResponseBody()

public data class GetTraceIdError(
    public val message: String? = null,
) : ResponseBody()

public class GetTraceIdResult private constructor(
    status: Int,
    body: ResponseBody?,
) : Result(status, body) {
    public constructor(success: Success) : this(200, success.response)

    public constructor(failure: Failure) : this(failure.statusCode.code, failure.response)

    public class Success(
        public val response: GetTraceIdResponse,
    )

    public class Failure(
        public val statusCode: StatusCode,
        public val response: GetTraceIdError,
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
