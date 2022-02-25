/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.request.phone

import cloud.pace.sdk.api.user.UserAPI
import cloud.pace.sdk.api.user.generated.model.*
import cloud.pace.sdk.api.utils.EnumConverterFactory
import cloud.pace.sdk.api.utils.InterceptorUtils
import cloud.pace.sdk.utils.toIso8601
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.JsonApiConverterFactory
import moe.banana.jsonapi2.Resource
import moe.banana.jsonapi2.ResourceAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

object UpdateUserPhoneAPI {

    interface UpdateUserPhoneService {
        /* Request a change of the users phone number */
        /* The endpoint will issue an email to the customer, to confirm the update of the phone number is valid. After confirmation by the user an SMS is send to the user to verify the phone number. The SMS contains a code, that needs to be provided to the [verify user phone](#operation/VerifyUserPhone) operation.
Mailing the customer will be omitted for the first time (if there is no phone number set).
If the process is not completed within 24h the process is canceled.
 */
        @PUT("user/phone")
        fun updateUserPhone(
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

    fun UserAPI.PhoneAPI.updateUserPhone(body: Body, readTimeout: Long? = null, additionalHeaders: Map<String, String>? = null): Call<ResponseBody> {
        val client = OkHttpClient.Builder()
                        .addNetworkInterceptor(InterceptorUtils.getInterceptor("application/vnd.api+json", "application/vnd.api+json", true, additionalHeaders))
                        .authenticator(InterceptorUtils.getAuthenticator())

        if (readTimeout != null) {
            client.readTimeout(readTimeout, TimeUnit.SECONDS)
        }

        val service: UpdateUserPhoneService =
            Retrofit.Builder()
                .client(client.build())
                .baseUrl(UserAPI.baseUrl)
                .addConverterFactory(EnumConverterFactory())
                .addConverterFactory(
                    JsonApiConverterFactory.create(
                        Moshi.Builder()
                            .add(ResourceAdapterFactory.builder()
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
                .create(UpdateUserPhoneService::class.java)

        return service.updateUserPhone(body)
    }
}
