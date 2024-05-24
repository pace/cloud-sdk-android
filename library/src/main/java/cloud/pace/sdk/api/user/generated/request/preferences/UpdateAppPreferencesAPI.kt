/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.request.preferences

import cloud.pace.sdk.api.request.BaseRequest
import cloud.pace.sdk.api.user.UserAPI
import cloud.pace.sdk.api.user.generated.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

object UpdateAppPreferencesAPI {

    interface UpdateAppPreferencesService {
        /* Update the users app preferences */
        /* Updates the apps preferences for the user identified by the given token.
The preferences may only be updated if the client has the respective scope
or is identified by the `clientId`. In order words all clients have their own
preferences namespace, only with the respective scope e.g. `user:preferences:read:xyz`
a client might be able to read the preferences of a different client.
The preferences should not have more than 10 key-value pairs per app.
Any key must not be longer than 128 bytes and any
value not longer than 10 megabytes (including complex json objects).
 */
        @JvmSuppressWildcards
        @PUT("preferences/{clientId}")
        fun updateAppPreferences(
            @HeaderMap headers: Map<String, String>,
            @Path("clientId") clientId: String? = null,
            @retrofit2.http.Body body: Map<String, Any>
        ): Call<ResponseBody>
    }

    open class Request : BaseRequest() {

        fun updateAppPreferences(
            clientId: String? = null,
            body: Map<String, Any>,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<ResponseBody> {
            val headers = headers(true, "application/json", "application/json", additionalHeaders)

            return retrofit(UserAPI.baseUrl, additionalParameters, readTimeout)
                .create(UpdateAppPreferencesService::class.java)
                .updateAppPreferences(
                    headers,
                    clientId,
                    body
                )
        }
    }

    fun UserAPI.PreferencesAPI.updateAppPreferences(
        clientId: String? = null,
        body: Map<String, Any>,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().updateAppPreferences(
        clientId,
        body,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
