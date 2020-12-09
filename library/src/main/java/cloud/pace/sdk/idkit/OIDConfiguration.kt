package cloud.pace.sdk.idkit

import net.openid.appauth.ResponseTypeValues

data class OIDConfiguration @JvmOverloads constructor(
    val authorizationEndpoint: String,
    val tokenEndpoint: String,
    val userInfoEndpoint: String? = null,
    val clientId: String,
    val clientSecret: String? = null,
    val scopes: List<String>? = null,
    val redirectUri: String,
    val responseType: String = ResponseTypeValues.CODE,
    val additionalParameters: Map<String, String>? = null
)
