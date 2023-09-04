/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.request.phone

import cloud.pace.sdk.api.request.BaseRequest
import cloud.pace.sdk.api.user.UserAPI
import cloud.pace.sdk.api.user.generated.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

object UpdateUserPhoneAPI {

    interface UpdateUserPhoneService {
        /* Request a change of the users phone number */
        /* The endpoint will issue an email to the customer, to confirm the update of the phone number is valid. After confirmation by the user an SMS is send to the user to verify the phone number. The SMS contains a code, that needs to be provided to the [verify user phone](#operation/VerifyUserPhone) operation.
Mailing the customer will be omitted for the first time (if there is no phone number set).
If the process is not completed within 24h the process is canceled.
 */
        @PUT("user/phone")
        fun updateUserPhone(
            @HeaderMap headers: Map<String, String>,
            @retrofit2.http.Body body: Body
        ): Call<ResponseBody>
    }

    /* The endpoint will issue an email to the customer, to confirm the update of the phone number is valid. After confirmation by the user an SMS is send to the user to verify the phone number. The SMS contains a code, that needs to be provided to the [verify user phone](#operation/VerifyUserPhone) operation.
    Mailing the customer will be omitted for the first time (if there is no phone number set).
    If the process is not completed within 24h the process is canceled.
     */
    class Body {

        var data: UpdateUserPhoneBody? = null
    }

    open class Request : BaseRequest() {

        fun updateUserPhone(
            body: Body,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<ResponseBody> {
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(UserAPI.baseUrl, additionalParameters, readTimeout)
                .create(UpdateUserPhoneService::class.java)
                .updateUserPhone(
                    headers,
                    body
                )
        }
    }

    fun UserAPI.PhoneAPI.updateUserPhone(
        body: Body,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().updateUserPhone(
        body,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
