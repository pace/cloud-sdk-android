package cloud.pace.sdk.idkit.model

import net.openid.appauth.ResponseTypeValues

data class OIDConfiguration @JvmOverloads constructor(
    val authorizationEndpoint: String = "https://id.pace.cloud/auth/realms/pace/protocol/openid-connect/auth",
    val endSessionEndpoint: String = "https://id.pace.cloud/auth/realms/pace/protocol/openid-connect/logout",
    val tokenEndpoint: String = "https://id.pace.cloud/auth/realms/pace/protocol/openid-connect/token",
    val userInfoEndpoint: String? = "https://id.pace.cloud/auth/realms/pace/protocol/openid-connect/userinfo",
    val clientId: String,
    val clientSecret: String? = null,
    val scopes: List<String>? = null,
    val redirectUri: String,
    val responseType: String = ResponseTypeValues.CODE,
    var additionalParameters: Map<String, String>? = null
) {

    companion object {

        @JvmOverloads
        fun development(
            clientId: String,
            clientSecret: String? = null,
            scopes: List<String>? = null,
            redirectUri: String,
            responseType: String = ResponseTypeValues.CODE,
            additionalParameters: Map<String, String>? = null
        ) = OIDConfiguration(
            authorizationEndpoint = "https://id.dev.pace.cloud/auth/realms/pace/protocol/openid-connect/auth",
            endSessionEndpoint = "https://id.dev.pace.cloud/auth/realms/pace/protocol/openid-connect/logout",
            tokenEndpoint = "https://id.dev.pace.cloud/auth/realms/pace/protocol/openid-connect/token",
            userInfoEndpoint = "https://id.dev.pace.cloud/auth/realms/pace/protocol/openid-connect/userinfo",
            clientId = clientId,
            clientSecret = clientSecret,
            scopes = scopes,
            redirectUri = redirectUri,
            responseType = responseType,
            additionalParameters = additionalParameters
        )

        @JvmOverloads
        fun sandbox(
            clientId: String,
            clientSecret: String? = null,
            scopes: List<String>? = null,
            redirectUri: String,
            responseType: String = ResponseTypeValues.CODE,
            additionalParameters: Map<String, String>? = null
        ) = OIDConfiguration(
            authorizationEndpoint = "https://id.sandbox.pace.cloud/auth/realms/pace/protocol/openid-connect/auth",
            endSessionEndpoint = "https://id.sandbox.pace.cloud/auth/realms/pace/protocol/openid-connect/logout",
            tokenEndpoint = "https://id.sandbox.pace.cloud/auth/realms/pace/protocol/openid-connect/token",
            userInfoEndpoint = "https://id.sandbox.pace.cloud/auth/realms/pace/protocol/openid-connect/userinfo",
            clientId = clientId,
            clientSecret = clientSecret,
            scopes = scopes,
            redirectUri = redirectUri,
            responseType = responseType,
            additionalParameters = additionalParameters
        )

        @JvmOverloads
        fun staging(
            clientId: String,
            clientSecret: String? = null,
            scopes: List<String>? = null,
            redirectUri: String,
            responseType: String = ResponseTypeValues.CODE,
            additionalParameters: Map<String, String>? = null
        ) = OIDConfiguration(
            authorizationEndpoint = "https://id.stage.pace.cloud/auth/realms/pace/protocol/openid-connect/auth",
            endSessionEndpoint = "https://id.stage.pace.cloud/auth/realms/pace/protocol/openid-connect/logout",
            tokenEndpoint = "https://id.stage.pace.cloud/auth/realms/pace/protocol/openid-connect/token",
            userInfoEndpoint = "https://id.stage.pace.cloud/auth/realms/pace/protocol/openid-connect/userinfo",
            clientId = clientId,
            clientSecret = clientSecret,
            scopes = scopes,
            redirectUri = redirectUri,
            responseType = responseType,
            additionalParameters = additionalParameters
        )

        @JvmOverloads
        fun production(
            clientId: String,
            clientSecret: String? = null,
            scopes: List<String>? = null,
            redirectUri: String,
            responseType: String = ResponseTypeValues.CODE,
            additionalParameters: Map<String, String>? = null
        ) = OIDConfiguration(
            clientId = clientId,
            clientSecret = clientSecret,
            scopes = scopes,
            redirectUri = redirectUri,
            responseType = responseType,
            additionalParameters = additionalParameters
        )
    }
}
