//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated.model.request

import kotlin.String

public data class GetAccessTokenRequest(
  /**
   * The reason for requesting a new access token. Currently the value can either be `unauthorized`
   * or `other`.
   */
  public val reason: String,
  /**
   * The token which was used before by the app.
   */
  public val oldToken: String?
)