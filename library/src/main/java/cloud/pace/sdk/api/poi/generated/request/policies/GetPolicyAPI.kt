/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.request.policies

import cloud.pace.sdk.api.poi.POIAPI
import cloud.pace.sdk.api.poi.generated.model.*
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.*

object GetPolicyAPI {

    interface GetPolicyService {
        /* Returns policy with specified id */
        /* Returns policy with specified id */
        @GET("policies/{policyId}")
        fun getPolicy(
            @HeaderMap headers: Map<String, String>,
            /* ID of the policy */
            @Path("policyId") policyId: String? = null
        ): Call<Policy>
    }

    open class Request : BaseRequest() {

        fun getPolicy(
            policyId: String? = null,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<Policy> {
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(POIAPI.baseUrl, additionalParameters, readTimeout)
                .create(GetPolicyService::class.java)
                .getPolicy(
                    headers,
                    policyId
                )
        }
    }

    fun POIAPI.PoliciesAPI.getPolicy(
        policyId: String? = null,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().getPolicy(
        policyId,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
