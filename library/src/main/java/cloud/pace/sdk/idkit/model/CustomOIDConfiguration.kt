package cloud.pace.sdk.idkit.model

import net.openid.appauth.ResponseTypeValues

data class CustomOIDConfiguration @JvmOverloads constructor(
    val clientId: String,
    val redirectUri: String,
    val authorizationEndpoint: String? = null,
    val endSessionEndpoint: String? = null,
    val tokenEndpoint: String? = null,
    val userInfoEndpoint: String? = null,
    val clientSecret: String? = null,
    val scopes: List<String>? = null,
    val responseType: String = ResponseTypeValues.CODE,
    var additionalParameters: Map<String, String>? = null
)
