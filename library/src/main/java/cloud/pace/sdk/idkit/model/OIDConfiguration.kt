package cloud.pace.sdk.idkit.model

import net.openid.appauth.ResponseTypeValues

data class OIDConfiguration @JvmOverloads constructor(
    val authorizationEndpoint: String,
    val endSessionEndpoint: String,
    val tokenEndpoint: String,
    val userInfoEndpoint: String? = null,
    val clientId: String,
    val clientSecret: String? = null,
    val scopes: List<String>? = null,
    val redirectUri: String,
    val responseType: String = ResponseTypeValues.CODE,
    var additionalParameters: Map<String, String>? = null
)