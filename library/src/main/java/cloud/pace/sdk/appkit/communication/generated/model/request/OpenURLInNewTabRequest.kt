//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated.model.request

/**
 * @param url The url to be opened
 * @param cancelUrl The url to be loaded if `url` cannot be opened.
 * @param integrated Whether the tab should be opened in the SDK's web view instead of the system's browser.
 */
public data class OpenURLInNewTabRequest(
    /**
     * The url to be opened
     */
    public val url: String,
    /**
     * The url to be loaded if `url` cannot be opened.
     */
    public val cancelUrl: String,
    /**
     * Whether the tab should be opened in the SDK's web view instead of the system's browser.
     */
    public val integrated: Boolean?,
)
