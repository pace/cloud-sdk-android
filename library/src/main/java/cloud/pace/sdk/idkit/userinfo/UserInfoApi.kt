package cloud.pace.sdk.idkit.userinfo

import cloud.pace.sdk.api.utils.InterceptorUtils
import cloud.pace.sdk.idkit.model.UserEndpointNotDefined
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.handleCallback
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import timber.log.Timber

interface UserInfoService {

    @GET(".")
    fun getUserInfo(@HeaderMap headers: Map<String, String>): Call<UserInfoResponse>
}

class UserInfoApiClient(private val userInfoEndpoint: String?) {

    fun getUserInfo(additionalHeaders: Map<String, String>? = null, additionalParameters: Map<String, String>? = null, completion: (Completion<UserInfoResponse>) -> Unit) {
        if (userInfoEndpoint != null) {
            val baseUrl = if (userInfoEndpoint.endsWith("/")) userInfoEndpoint else "$userInfoEndpoint/"

            val headers = InterceptorUtils.getHeaders(true, "application/json", "application/json", additionalHeaders)
            val service = Retrofit.Builder()
                .client(OkHttpClient.Builder().addInterceptor(InterceptorUtils.getInterceptor(additionalParameters)).build())
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UserInfoService::class.java)

            service.getUserInfo(headers).handleCallback(completion)
        } else {
            val throwable = UserEndpointNotDefined
            Timber.e(throwable)
            completion(Failure(throwable))
        }
    }
}
