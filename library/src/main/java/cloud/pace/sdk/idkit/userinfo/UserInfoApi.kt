package cloud.pace.sdk.idkit.userinfo

import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.utils.InterceptorUtils
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.handleCallback
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap

interface UserInfoService {
    @GET(".")
    fun getUserInfo(@HeaderMap headers: Map<String, String>): Call<UserInfoResponse>
}

class UserInfoApiClient(userInfoEndpoint: String, accessToken: String, val additionalHeaders: Map<String, String>? = null, additionalParameters: Map<String, String>? = null) {
    private val service = create(userInfoEndpoint, accessToken, additionalParameters)

    fun getUserInfo(completion: (Completion<UserInfoResponse>) -> Unit) {
        service.getUserInfo(InterceptorUtils.getHeaders(true, "application/json", "application/json", additionalHeaders)).handleCallback(completion)
    }

    companion object {
        private fun create(userInfoUrl: String, accessToken: String, additionalParameters: Map<String, String>? = null): UserInfoService {
            API.addAuthorizationHeader(accessToken)
            val baseUrl = if (userInfoUrl.endsWith("/")) userInfoUrl else "$userInfoUrl/"

            return Retrofit.Builder()
                .client(OkHttpClient.Builder().addInterceptor(InterceptorUtils.getInterceptor(additionalParameters)).build())
                .baseUrl(baseUrl)
                .addConverterFactory(
                    MoshiConverterFactory.create(
                        Moshi.Builder()
                            .add(KotlinJsonAdapterFactory())
                            .build()
                    )
                )
                .build()
                .create(UserInfoService::class.java)
        }
    }
}
