package cloud.pace.sdk.api.interceptor

import cloud.pace.sdk.api.utils.RequestUtils
import cloud.pace.sdk.api.utils.RequestUtils.BEARER
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.resume
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationException
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection

class AuthenticationInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        return if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED && IDKit.isInitialized && IDKit.isAuthorizationValid()) {
            val oldToken = IDKit.cachedToken()
            // Make sure that the token is only refreshed once for multiple requests
            synchronized(this) {
                try {
                    val cachedToken = IDKit.cachedToken()
                    val newToken = if (oldToken == cachedToken) {
                        runBlocking {
                            getNewToken()
                        }
                    } else {
                        // Token has already been refreshed from another request. Use the cached token
                        cachedToken
                    }

                    if (newToken != null) {
                        // Close previous response body
                        response.body?.close()
                        chain.proceed(request.newBuilder().header(RequestUtils.AUTHORIZATION_HEADER, "$BEARER $newToken").build())
                    } else {
                        response
                    }
                } catch (e: Exception) {
                    if (e == AuthorizationException.GeneralErrors.JSON_DESERIALIZATION_ERROR ||
                        e == AuthorizationException.GeneralErrors.NETWORK_ERROR ||
                        e == AuthorizationException.GeneralErrors.SERVER_ERROR
                    ) {
                        response.newBuilder().code(HttpURLConnection.HTTP_UNAVAILABLE).build()
                    } else {
                        response
                    }
                }
            }
        } else {
            response
        }
    }

    private suspend fun getNewToken() = suspendCancellableCoroutine<String?> { continuation ->
        IDKit.refreshToken {
            continuation.resume(it)
        }
    }
}
