//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated.model.request

/**
 * @param key The key of the event
 * @param parameters Dictionary of additional event parameters
 * @param context Provide additional context for the SDK which will not be logged
 */
public data class LogEventRequest(
    /**
     * The key of the event
     */
    public val key: String,
    /**
     * Dictionary of additional event parameters
     */
    public val parameters: Map<String, Any>?,
    /**
     * Provide additional context for the SDK which will not be logged
     */
    public val context: Map<String, Any>?,
)
