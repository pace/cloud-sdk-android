/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.request.credentials

import cloud.pace.sdk.api.request.BaseRequest
import cloud.pace.sdk.api.user.UserAPI
import cloud.pace.sdk.api.user.generated.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

object CheckUserPINAPI {

    interface CheckUserPINService {
        /* Check if user has a PIN */
        /* This call can be used to check if the user PIN is set.
 */
        @GET("user/pin")
        fun checkUserPIN(
            @HeaderMap headers: Map<String, String>,
            /* Timeout in seconds, wait until PIN is set (long polling) */
            @Query("timeout") timeout: Int? = null
        ): Call<ResponseBody>
    }

    open class Request : BaseRequest() {

        fun checkUserPIN(
            timeout: Int? = null,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<ResponseBody> {
            val headers = headers(true, "application/json", "application/json", additionalHeaders)

            return retrofit(UserAPI.baseUrl, additionalParameters, readTimeout)
                .create(CheckUserPINService::class.java)
                .checkUserPIN(
                    headers,
                    timeout
                )
        }
    }

    fun UserAPI.CredentialsAPI.checkUserPIN(
        timeout: Int? = null,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().checkUserPIN(
        timeout,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
