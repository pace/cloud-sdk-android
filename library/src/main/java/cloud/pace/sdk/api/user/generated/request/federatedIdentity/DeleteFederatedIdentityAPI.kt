/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.request.federatedIdentity

import cloud.pace.sdk.api.request.BaseRequest
import cloud.pace.sdk.api.user.UserAPI
import cloud.pace.sdk.api.user.generated.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

object DeleteFederatedIdentityAPI {

    interface DeleteFederatedIdentityService {
        /* Delete federated identity */
        /* Delete federated identity for the user with the given identity provider
 */
        @DELETE("federated-identities/{identityProvider}")
        fun deleteFederatedIdentity(
            @HeaderMap headers: Map<String, String>,
            @Path("identityProvider") identityProvider: String? = null
        ): Call<ResponseBody>
    }

    open class Request : BaseRequest() {

        fun deleteFederatedIdentity(
            identityProvider: String? = null,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<ResponseBody> {
            val headers = headers(true, "application/json", "application/json", additionalHeaders)

            return retrofit(UserAPI.baseUrl, additionalParameters, readTimeout)
                .create(DeleteFederatedIdentityService::class.java)
                .deleteFederatedIdentity(
                    headers,
                    identityProvider
                )
        }
    }

    fun UserAPI.FederatedIdentityAPI.deleteFederatedIdentity(
        identityProvider: String? = null,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().deleteFederatedIdentity(
        identityProvider,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}