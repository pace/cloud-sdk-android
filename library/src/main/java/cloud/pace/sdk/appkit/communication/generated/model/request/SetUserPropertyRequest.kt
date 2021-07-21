//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated.model.request

import kotlin.Boolean
import kotlin.String

public data class SetUserPropertyRequest(
  /**
   * The key for the user property
   */
  public val key: String,
  /**
   * The value to set the user property to
   */
  public val `value`: String,
  /**
   * If true, an already existing peroperty with the same key will be overwritten
   */
  public val update: Boolean?
)