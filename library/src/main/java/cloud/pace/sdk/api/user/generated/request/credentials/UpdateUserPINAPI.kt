/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.request.credentials

import cloud.pace.sdk.api.user.UserAPI
import cloud.pace.sdk.api.user.generated.model.UserPINBody
import cloud.pace.sdk.api.utils.EnumConverterFactory
import cloud.pace.sdk.api.utils.InterceptorUtils
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import moe.banana.jsonapi2.JsonApiConverterFactory
import moe.banana.jsonapi2.ResourceAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.Date
import java.util.concurrent.TimeUnit

object UpdateUserPINAPI {

    interface UpdateUserPINService {
        /* Set the new PIN */
        /* Sets the PIN of the user. The user has to select a secure PIN, this is ensured via rules. If one of the rules fails `406` is returned. To set the PIN an account OTP needs to be provided.
The following rules apply to verify the PIN:
* must be 4 digits long
* must use 3 different digits
* must not be a numerical series (e.g. 1234, 4321, ...)
 */
        @PUT("user/pin")
        fun updateUserPIN(
            @HeaderMap headers: Map<String, String>,
            @retrofit2.http.Body body: Body
        ): Call<ResponseBody>
    }

    /* Sets the PIN of the user. The user has to select a secure PIN, this is ensured via rules. If one of the rules fails `406` is returned. To set the PIN an account OTP needs to be provided.
    The following rules apply to verify the PIN:
    * must be 4 digits long
    * must use 3 different digits
    * must not be a numerical series (e.g. 1234, 4321, ...)
     */
    class Body {

        var data: UserPINBody? = null
    }

    fun UserAPI.CredentialsAPI.updateUserPIN(
        body: Body,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ): Call<ResponseBody> {
        val client = OkHttpClient.Builder().addInterceptor(InterceptorUtils.getInterceptor(additionalParameters))
        val headers = InterceptorUtils.getHeaders(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

        if (readTimeout != null) {
            client.readTimeout(readTimeout, TimeUnit.SECONDS)
        }

        val service: UpdateUserPINService =
            Retrofit.Builder()
                .client(client.build())
                .baseUrl(UserAPI.baseUrl)
                .addConverterFactory(EnumConverterFactory())
                .addConverterFactory(
                    JsonApiConverterFactory.create(
                        Moshi.Builder()
                            .add(
                                ResourceAdapterFactory.builder()
                                    .build()
                            )
                            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                            .add(KotlinJsonAdapterFactory())
                            .build()
                    )
                )
                .addConverterFactory(
                    MoshiConverterFactory.create(
                        Moshi.Builder()
                            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                            .add(KotlinJsonAdapterFactory())
                            .build()
                    )
                )
                .build()
                .create(UpdateUserPINService::class.java)

        return service.updateUserPIN(
            headers,
            body
        )
    }
}
