/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.request.federatedIdentity

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
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

object SetFederatedIdentityAPI {

    interface SetFederatedIdentityService {
        /* Define federated identity */
        /* Set a federated identity for the user with the given identity provider
 */
        @PUT("federated-identities/{identityProvider}")
        fun setFederatedIdentity(
            @Path("identityProvider") identityProvider: String? = null
        ): Call<Void>
    }

    fun UserAPI.FederatedIdentityAPI.setFederatedIdentity(identityProvider: String? = null, readTimeout: Long? = null): Call<Void> {
        val service: SetFederatedIdentityService =
            Retrofit.Builder()
                .client(OkHttpClient.Builder()
                    .addNetworkInterceptor(InterceptorUtils.getInterceptor("application/json", "application/json", true))
                    .authenticator(InterceptorUtils.getAuthenticator())
                    .readTimeout(readTimeout ?: 10L, TimeUnit.SECONDS)
                    .build()
                )
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
                .create(SetFederatedIdentityService::class.java)    

        return service.setFederatedIdentity(identityProvider)
    }
}
