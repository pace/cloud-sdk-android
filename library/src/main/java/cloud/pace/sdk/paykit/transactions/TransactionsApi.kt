package cloud.pace.sdk.paykit.transactions

import cloud.pace.sdk.poikit.utils.ApiException
import cloud.pace.sdk.utils.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

interface TransactionsService {
    @GET("pay/beta/transactions")
    fun getTransactions(
        @Query("sort") sortBy: String?
    ): Call<TransactionsResponse>
}

class TransactionsApiClient(environment: Environment, accessToken: String) {

    private val service = create(environment, accessToken)

    fun getTransactions(sortBy: String? = "-createdAt", completion: (Completion<TransactionsResponse>) -> Unit) {
        service.getTransactions(sortBy).enqueue {
            onResponse = {
                val body = it.body()
                if (it.isSuccessful && body != null) {
                    completion(Success(body))
                } else {
                    completion(Failure(ApiException(it.code(), it.message())))
                }
            }

            onFailure = {
                completion(Failure(it ?: Exception("Unknown exception")))
            }
        }
    }

    companion object {

        private fun create(environment: Environment, accessToken: String): TransactionsService {
            return Retrofit.Builder()
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor {
                            it.proceed(
                                it.request()
                                    .newBuilder()
                                    .header(ApiUtils.ACCEPT_HEADER, "application/vnd.api+json")
                                    .header(ApiUtils.CONTENT_TYPE_HEADER, "application/vnd.api+json")
                                    .header(ApiUtils.USER_AGENT_HEADER, ApiUtils.getUserAgent())
                                    .header(ApiUtils.AUTHORIZATION_HEADER, "Bearer $accessToken")
                                    .build()
                            )
                        }
                        .build())
                .baseUrl(environment.apiUrl)
                .addConverterFactory(
                    MoshiConverterFactory.create(
                        Moshi.Builder()
                            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                            .add(KotlinJsonAdapterFactory())
                            .build()
                    )
                )
                .build()
                .create(TransactionsService::class.java)
        }
    }
}
