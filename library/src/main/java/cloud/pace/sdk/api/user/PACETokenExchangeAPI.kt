package cloud.pace.sdk.api.user

import cloud.pace.sdk.api.request.BaseRequest
import cloud.pace.sdk.utils.URL
import com.squareup.moshi.Json
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.HeaderMap
import retrofit2.http.POST

object PACETokenExchangeAPI {

    data class TokenExchangeResponse(@Json(name = "access_token") val accessToken: String)

    interface PACETokenExchangeService {
        @FormUrlEncoded
        @POST("/auth/realms/pace/protocol/openid-connect/token")
        fun tokenExchange(
            @HeaderMap headers: Map<String, String>,
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String,
            @Field("grant_type") grantType: String,
            @Field("subject_issuer") subjectIssuer: String,
            @Field("subject_token") subjectToken: String,
            @Field("subject_token_type") subjectTokenType: String
        ): Call<TokenExchangeResponse>
    }

    open class Request : BaseRequest() {
        fun tokenExchange(
            clientId: String,
            clientSecret: String,
            grantType: String,
            subjectIssuer: String,
            subjectToken: String,
            subjectTokenType: String
        ): Call<TokenExchangeResponse> {
            val headers = headers(false, "application/x-www-form-urlencoded", "application/x-www-form-urlencoded")

            return retrofit(URL.paceID)
                .create(PACETokenExchangeService::class.java)
                .tokenExchange(
                    headers = headers,
                    clientId = clientId,
                    clientSecret = clientSecret,
                    grantType = grantType,
                    subjectIssuer = subjectIssuer,
                    subjectToken = subjectToken,
                    subjectTokenType = subjectTokenType
                )
        }
    }

    fun tokenExchange(
        clientId: String,
        clientSecret: String,
        grantType: String = "urn:ietf:params:oauth:grant-type:token-exchange",
        subjectIssuer: String,
        subjectToken: String,
        subjectTokenType: String = "urn:ietf:params:oauth:token-type:access_token"
    ) = Request().tokenExchange(
        clientId = clientId,
        clientSecret = clientSecret,
        grantType = grantType,
        subjectIssuer = subjectIssuer,
        subjectToken = subjectToken,
        subjectTokenType = subjectTokenType
    )
}
