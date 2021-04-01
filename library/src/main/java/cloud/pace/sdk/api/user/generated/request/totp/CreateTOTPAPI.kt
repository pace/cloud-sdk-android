/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.request.totp

import cloud.pace.sdk.api.user.UserAPI
import cloud.pace.sdk.api.user.generated.model.DeviceTOTP
import cloud.pace.sdk.api.user.generated.model.DeviceTOTPBody
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
import retrofit2.http.POST
import java.util.*

object CreateTOTPAPI {

    interface CreateTOTPService {
        /* Create device TOTP */
        /* Creates a device TOTP token using either PIN, password or an user OTP. In case the PIN, password or OTP is invalid `403` is returned. If multiple values are provided first the OTP is checked, then the password, then the PIN. In case the one of the provided values is correct, a TOTP will be created.
 */
        @POST("user/devices/totp")
        fun createTOTP(
            @retrofit2.http.Body body: Body
        ): Call<DeviceTOTP>
    }

    /* Creates a device TOTP token using either PIN, password or an user OTP. In case the PIN, password or OTP is invalid `403` is returned. If multiple values are provided first the OTP is checked, then the password, then the PIN. In case the one of the provided values is correct, a TOTP will be created.
     */
    class Body {

        var data: DeviceTOTPBody? = null
    }

    private val service: CreateTOTPService by lazy {
        Retrofit.Builder()
            .client(OkHttpClient.Builder().addNetworkInterceptor(InterceptorUtils.getInterceptor("application/vnd.api+json", "application/vnd.api+json")).build())
            .baseUrl(UserAPI.baseUrl)
            .addConverterFactory(EnumConverterFactory())
            .addConverterFactory(
                JsonApiConverterFactory.create(
                    Moshi.Builder()
                        .add(ResourceAdapterFactory.builder().build())
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
            .create(CreateTOTPService::class.java)
    }

    fun UserAPI.TOTPAPI.createTOTP(body: Body) =
        service.createTOTP(body)
}
