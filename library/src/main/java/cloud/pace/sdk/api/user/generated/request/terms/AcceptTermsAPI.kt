/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.request.terms

import cloud.pace.sdk.api.user.UserAPI
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
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.Date
import java.util.concurrent.TimeUnit

object AcceptTermsAPI {

    interface AcceptTermsService {
        /* Accepts the terms presented under the UUID */
        /* Accepts the terms using the user that is linked in the token.
This endpoint must only be called as a reaction of direct user
consent with the terms of service.
 */
        @POST("terms/{termsId}/accept")
        fun acceptTerms(
            @HeaderMap headers: Map<String, String>,
            @Path("termsId") termsId: String? = null
        ): Call<ResponseBody>
    }

    fun UserAPI.TermsAPI.acceptTerms(
        termsId: String? = null,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ): Call<ResponseBody> {
        val client = OkHttpClient.Builder().addInterceptor(InterceptorUtils.getInterceptor(additionalParameters))
        val headers = InterceptorUtils.getHeaders(true, "application/json", "application/json", additionalHeaders)

        if (readTimeout != null) {
            client.readTimeout(readTimeout, TimeUnit.SECONDS)
        }

        val service: AcceptTermsService =
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
                .create(AcceptTermsService::class.java)

        return service.acceptTerms(
            headers,
            termsId
        )
    }
}
