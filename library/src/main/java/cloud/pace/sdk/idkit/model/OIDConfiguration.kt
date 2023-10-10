package cloud.pace.sdk.idkit.model

import android.net.Uri
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

data class OIDConfiguration @JvmOverloads constructor(
    val authorizationEndpoint: String = "https://id.pace.cloud/auth/realms/pace/protocol/openid-connect/auth",
    val endSessionEndpoint: String = "https://id.pace.cloud/auth/realms/pace/protocol/openid-connect/logout",
    val tokenEndpoint: String = "https://id.pace.cloud/auth/realms/pace/protocol/openid-connect/token",
    val userInfoEndpoint: String? = "https://id.pace.cloud/auth/realms/pace/protocol/openid-connect/userinfo",
    val clientSecret: String? = null,
    val scopes: List<String>? = null,
    val redirectUri: String,
    val responseType: String = ResponseTypeValues.CODE,
    var additionalParameters: Map<String, String>? = null,
    val integrated: Boolean = false
)

fun OIDConfiguration.toAuthorizationServiceConfiguration() = AuthorizationServiceConfiguration(
    Uri.parse(authorizationEndpoint),
    Uri.parse(tokenEndpoint),
    null,
    Uri.parse(endSessionEndpoint)
)
