package cloud.pace.sdk.idkit.model

import cloud.pace.sdk.utils.Environment
import net.openid.appauth.ResponseTypeValues

data class CustomOIDConfiguration @JvmOverloads constructor(
    val redirectUri: String,
    val authorizationEndpoint: String? = null,
    val endSessionEndpoint: String? = null,
    val tokenEndpoint: String? = null,
    val userInfoEndpoint: String? = null,
    val clientSecret: String? = null,
    val scopes: List<String>? = null,
    val responseType: String = ResponseTypeValues.CODE,
    var additionalParameters: Map<String, String>? = null,
    val integrated: Boolean = false,
    val tokenExchangeConfig: TokenExchangeConfiguration? = null
)

data class TokenExchangeConfiguration(val issuerId: String, val clientId: String, val clientSecret: String)

fun CustomOIDConfiguration.oidConfiguration(environment: Environment) =
    when (environment) {
        Environment.PRODUCTION -> OIDConfiguration(
            authorizationEndpoint = authorizationEndpoint ?: "https://id.pace.cloud/auth/realms/pace/protocol/openid-connect/auth",
            endSessionEndpoint = endSessionEndpoint ?: "https://id.pace.cloud/auth/realms/pace/protocol/openid-connect/logout",
            tokenEndpoint = tokenEndpoint ?: "https://id.pace.cloud/auth/realms/pace/protocol/openid-connect/token",
            userInfoEndpoint = userInfoEndpoint ?: "https://id.pace.cloud/auth/realms/pace/protocol/openid-connect/userinfo",
            clientSecret = clientSecret,
            scopes = scopes,
            redirectUri = redirectUri,
            responseType = responseType,
            additionalParameters = additionalParameters,
            integrated = integrated,
            tokenExchangeConfig = tokenExchangeConfig
        )
        Environment.SANDBOX -> OIDConfiguration(
            authorizationEndpoint = authorizationEndpoint ?: "https://id.sandbox.pace.cloud/auth/realms/pace/protocol/openid-connect/auth",
            endSessionEndpoint = endSessionEndpoint ?: "https://id.sandbox.pace.cloud/auth/realms/pace/protocol/openid-connect/logout",
            tokenEndpoint = tokenEndpoint ?: "https://id.sandbox.pace.cloud/auth/realms/pace/protocol/openid-connect/token",
            userInfoEndpoint = userInfoEndpoint ?: "https://id.sandbox.pace.cloud/auth/realms/pace/protocol/openid-connect/userinfo",
            clientSecret = clientSecret,
            scopes = scopes,
            redirectUri = redirectUri,
            responseType = responseType,
            additionalParameters = additionalParameters,
            integrated = integrated,
            tokenExchangeConfig = tokenExchangeConfig
        )
        Environment.DEVELOPMENT -> OIDConfiguration(
            authorizationEndpoint = authorizationEndpoint ?: "https://id.dev.pace.cloud/auth/realms/pace/protocol/openid-connect/auth",
            endSessionEndpoint = endSessionEndpoint ?: "https://id.dev.pace.cloud/auth/realms/pace/protocol/openid-connect/logout",
            tokenEndpoint = tokenEndpoint ?: "https://id.dev.pace.cloud/auth/realms/pace/protocol/openid-connect/token",
            userInfoEndpoint = userInfoEndpoint ?: "https://id.dev.pace.cloud/auth/realms/pace/protocol/openid-connect/userinfo",
            clientSecret = clientSecret,
            scopes = scopes,
            redirectUri = redirectUri,
            responseType = responseType,
            additionalParameters = additionalParameters,
            integrated = integrated,
            tokenExchangeConfig = tokenExchangeConfig
        )
    }
