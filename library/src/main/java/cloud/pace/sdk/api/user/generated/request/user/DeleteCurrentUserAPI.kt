/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.request.user

import cloud.pace.sdk.api.request.BaseRequest
import cloud.pace.sdk.api.user.UserAPI
import cloud.pace.sdk.api.user.generated.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

object DeleteCurrentUserAPI {

    interface DeleteCurrentUserService {
        /* Deletes the current account */
        /* The user deletion is implemented according to GDPR regulation.
An account OTP is required to perform the action.
 */
        @DELETE("user")
        fun deleteCurrentUser(
            @HeaderMap headers: Map<String, String>,
            @retrofit2.http.Body body: PlainOTP
        ): Call<ResponseBody>
    }

    open class Request : BaseRequest() {

        fun deleteCurrentUser(
            body: PlainOTP,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<ResponseBody> {
            val headers = headers(true, "application/json", "application/json", additionalHeaders)

            return retrofit(UserAPI.baseUrl, additionalParameters, readTimeout)
                .create(DeleteCurrentUserService::class.java)
                .deleteCurrentUser(
                    headers,
                    body
                )
        }
    }

    fun UserAPI.UserAPI.deleteCurrentUser(
        body: PlainOTP,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().deleteCurrentUser(
        body,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
