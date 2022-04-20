/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.request.credentials

import cloud.pace.sdk.api.user.UserAPI
import cloud.pace.sdk.api.user.generated.model.PinOrPassword
import cloud.pace.sdk.api.utils.EnumConverterFactory
import cloud.pace.sdk.api.utils.InterceptorUtils
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import moe.banana.jsonapi2.JsonApiConverterFactory
import moe.banana.jsonapi2.ResourceAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import java.util.Date
import java.util.concurrent.TimeUnit

object CheckUserPinOrPasswordAPI {

    interface CheckUserPinOrPasswordService {
        /* Check if user has a PIN or Password */
        /* This call can be used to check if the user PIN or password is set and verified.
 */
        @GET("user/pin-or-password")
        fun checkUserPinOrPassword(
            @HeaderMap headers: Map<String, String>,
        ): Call<PinOrPassword>
    }

    fun UserAPI.CredentialsAPI.checkUserPinOrPassword(
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ): Call<PinOrPassword> {
        val client = OkHttpClient.Builder().addInterceptor(InterceptorUtils.getInterceptor(additionalParameters))
        val headers = InterceptorUtils.getHeaders(true, "application/json", "application/json", additionalHeaders)

        if (readTimeout != null) {
            client.readTimeout(readTimeout, TimeUnit.SECONDS)
        }

        val service: CheckUserPinOrPasswordService =
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
                .create(CheckUserPinOrPasswordService::class.java)

        return service.checkUserPinOrPassword(
            headers
        )
    }
}
