/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.request.totp

import cloud.pace.sdk.api.request.BaseRequest
import cloud.pace.sdk.api.user.UserAPI
import cloud.pace.sdk.api.user.generated.model.*
import retrofit2.Call
import retrofit2.http.*

object CreateOTPAPI {

    interface CreateOTPService {
        /* Create OTP */
        /* Verifies that the passed PIN or password is valid for the user account and generates a one time password (OTP). One of the provided values need to be valid. First check is on the password, 2nd on the PIN.
 */
        @POST("user/otp")
        fun createOTP(
            @HeaderMap headers: Map<String, String>,
            @retrofit2.http.Body body: CreateOTP
        ): Call<CreateOTP>
    }

    open class Request : BaseRequest() {

        fun createOTP(
            body: CreateOTP,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<CreateOTP> {
            val headers = headers(true, "application/json", "application/json", additionalHeaders)

            return retrofit(UserAPI.baseUrl, additionalParameters, readTimeout)
                .create(CreateOTPService::class.java)
                .createOTP(
                    headers,
                    body
                )
        }
    }

    fun UserAPI.TOTPAPI.createOTP(
        body: CreateOTP,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().createOTP(
        body,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
