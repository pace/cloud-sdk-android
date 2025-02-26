//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated.model.response

public data class Response(
    public val id: String?,
    public val status: Int?,
    public val `header`: Map<String, Any>?,
    public val body: ResponseBody?,
)

public open class ResponseBody

public open class Result(
    public val status: Int,
    public val body: ResponseBody?,
)

public data class Message(
    public val message: String?,
) : ResponseBody()
