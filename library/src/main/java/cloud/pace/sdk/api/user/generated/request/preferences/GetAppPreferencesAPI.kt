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
import retrofit2.Call
import retrofit2.http.*

object GetAppPreferencesAPI {

    interface GetAppPreferencesService {
        /* Get the users app preferences */
        /* Requests the apps preferences for the user identified by the given token.
The preferences may only be returned if the client has the respective scope
or is identified by the `clientId`. In order words all clients have their own
preferences namespace, only with the respective scope e.g. `user:preferences:read:xyz`
a client might be able to read the preferences of a different client.
In case no preferences were ever set an empty object `{}` is returned.
 */
        @GET("preferences/{clientId}")
        fun getAppPreferences(
            @HeaderMap headers: Map<String, String>,
            @Path("clientId") clientId: String? = null
        ): Call<Map<String, Any>>
    }

    open class Request : BaseRequest() {

        fun getAppPreferences(
            clientId: String? = null,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<Map<String, Any>> {
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(UserAPI.baseUrl, additionalParameters, readTimeout)
                .create(GetAppPreferencesService::class.java)
                .getAppPreferences(
                    headers,
                    clientId
                )
        }
    }

    fun UserAPI.PreferencesAPI.getAppPreferences(
        clientId: String? = null,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().getAppPreferences(
        clientId,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
