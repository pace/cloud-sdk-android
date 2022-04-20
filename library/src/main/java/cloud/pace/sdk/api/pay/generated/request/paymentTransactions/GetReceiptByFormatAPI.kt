/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.paymentTransactions

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.utils.EnumConverterFactory
import cloud.pace.sdk.api.utils.InterceptorUtils
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
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
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.Date
import java.util.concurrent.TimeUnit

object GetReceiptByFormatAPI {

    interface GetReceiptByFormatService {
        /* Get receipt (download,file) for a single transaction in given file format */
        /* Provides the receipt that has already been sent via email (when processing the payment) as download in the provided file format.
 */
        @GET("receipts/{transactionID}.{fileFormat}")
        fun getReceiptByFormat(
            @HeaderMap headers: Map<String, String>,
            /* ID of the payment transaction */
            @Path("transactionID") transactionID: String,
            /* Format of the expected file */
            @Path("fileFormat") fileFormat: FileFormat? = null,
            /* (Optional) Specify the language you want the returned receipt to be localized in.
Returns the receipt in the default language that is available if the specified language is not available.
Language does not have to be valid language. For example, `language=local` means that the receipt should be displayed
in the language that is determined to be spoken in the area that the point of intereset at which the receipt has been generated at.
*Prefer using the `Accept-Language` header if you use this endpoint on an end-user device.*
 */
            @Query("language") language: String? = null
        ): Call<ResponseBody>
    }

    /* Format of the expected file */
    enum class FileFormat(val value: String) {
        @SerializedName("png")
        @Json(name = "png")
        PNG("png"),

        @SerializedName("pdf")
        @Json(name = "pdf")
        PDF("pdf")
    }

    fun PayAPI.PaymentTransactionsAPI.getReceiptByFormat(
        transactionID: String,
        fileFormat: FileFormat? = null,
        language: String? = null,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ): Call<ResponseBody> {
        val client = OkHttpClient.Builder().addInterceptor(InterceptorUtils.getInterceptor(additionalParameters))
        val headers = InterceptorUtils.getHeaders(true, "application/json", "application/json", additionalHeaders)

        if (readTimeout != null) {
            client.readTimeout(readTimeout, TimeUnit.SECONDS)
        }

        val service: GetReceiptByFormatService =
            Retrofit.Builder()
                .client(client.build())
                .baseUrl(PayAPI.baseUrl)
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
                .create(GetReceiptByFormatService::class.java)

        return service.getReceiptByFormat(
            headers,
            transactionID,
            fileFormat,
            language
        )
    }
}
