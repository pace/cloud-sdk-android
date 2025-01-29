//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated.model.response

public data class IsBiometricAuthEnabledResponse(
    public val isEnabled: Boolean,
) : ResponseBody()

public data class IsBiometricAuthEnabledError(
    public val message: String? = null,
) : ResponseBody()

public class IsBiometricAuthEnabledResult private constructor(
    status: Int,
    body: ResponseBody?,
) : Result(status, body) {
    public constructor(success: Success) : this(200, success.response)

    public constructor(failure: Failure) : this(failure.statusCode.code, failure.response)

    public class Success(
        public val response: IsBiometricAuthEnabledResponse,
    )

    public class Failure(
        public val statusCode: StatusCode,
        public val response: IsBiometricAuthEnabledError,
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
