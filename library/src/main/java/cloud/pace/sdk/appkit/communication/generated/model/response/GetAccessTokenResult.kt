//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated.model.response

import kotlin.Boolean
import kotlin.Int
import kotlin.String

public data class GetAccessTokenResponse(
  public val accessToken: String,
  public val isInitialToken: Boolean?
) : ResponseBody()

public data class GetAccessTokenError(
  public val message: String? = null
) : ResponseBody()

public class GetAccessTokenResult private constructor(
  status: Int,
  body: ResponseBody?
) : Result(status, body) {
  public constructor(success: Success) : this(200, success.response)

  public constructor(failure: Failure) : this(failure.statusCode.code, failure.response)

  public class Success(
    public val response: GetAccessTokenResponse
  )

  public class Failure(
    public val statusCode: StatusCode,
    public val response: GetAccessTokenError
  ) {
    public enum class StatusCode(
      public val code: Int
    ) {
      BadRequest(400),
      NotFound(404),
      RequestTimeout(408),
      InternalServerError(500),
      ;
    }
  }
}