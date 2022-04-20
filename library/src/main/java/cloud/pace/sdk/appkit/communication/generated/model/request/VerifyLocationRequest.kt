//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated.model.request

public data class VerifyLocationRequest(
    /**
     * The latitude of the location to be verified.
     */
    public val lat: Double,
    /**
     * The longitude of the location to be verified.
     */
    public val lon: Double,
    /**
     * The maximum location offset / inaccuracy in meters.
     */
    public val threshold: Double
)
