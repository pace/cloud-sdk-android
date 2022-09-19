package cloud.pace.sdk.api.request

import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.converter.EnumConverterFactory
import cloud.pace.sdk.api.interceptor.AuthenticationInterceptor
import cloud.pace.sdk.api.interceptor.QueryParametersInterceptor
import cloud.pace.sdk.api.utils.RequestUtils
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import moe.banana.jsonapi2.JsonApiConverterFactory
import moe.banana.jsonapi2.Resource
import moe.banana.jsonapi2.ResourceAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Date
import java.util.concurrent.TimeUnit

open class BaseRequest {

    open fun okHttpClient(additionalParameters: Map<String, String>? = null, readTimeout: Long? = null): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(QueryParametersInterceptor(additionalParameters))
            .addInterceptor(authenticationInterceptor())

        if (readTimeout != null) {
            builder.readTimeout(readTimeout, TimeUnit.SECONDS)
        }

        return builder.build()
    }

    open fun authenticationInterceptor(): Interceptor {
        return AuthenticationInterceptor()
    }

    open fun headers(isAuthorizationRequired: Boolean, contentType: String, accept: String? = null, additionalHeaders: Map<String, String>? = null): Map<String, String> {
        val headers = API.additionalHeaders.toMutableMap()
        if (!isAuthorizationRequired) {
            // Only send the authorization header when authorization is required
            headers.remove(RequestUtils.AUTHORIZATION_HEADER)
        }

        if (accept != null) headers[RequestUtils.ACCEPT_HEADER] = accept
        headers[RequestUtils.CONTENT_TYPE_HEADER] = contentType
        headers[RequestUtils.API_KEY_HEADER] = API.apiKey
        headers[RequestUtils.UBER_TRACE_ID_HEADER] = RequestUtils.getUberTraceId()
        headers[RequestUtils.USER_AGENT_HEADER] = PACECloudSDK.getBaseUserAgent()

        // Additional headers of the request have priority over the globally set headers, so set them last to override any existing header
        if (!additionalHeaders.isNullOrEmpty()) {
            headers += additionalHeaders
        }

        return headers
    }

    open fun retrofit(baseUrl: String, additionalParameters: Map<String, String>? = null, readTimeout: Long? = null, resources: List<Class<out Resource>>? = null): Retrofit {
        val resourceAdapterFactory = ResourceAdapterFactory.builder()
        resources?.forEach {
            resourceAdapterFactory.add(it)
        }

        return Retrofit.Builder()
            .client(okHttpClient(additionalParameters, readTimeout))
            .baseUrl(baseUrl)
            .addConverterFactory(EnumConverterFactory())
            .addConverterFactory(
                JsonApiConverterFactory.create(
                    Moshi.Builder()
                        .add(resourceAdapterFactory.build())
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
    }
}
