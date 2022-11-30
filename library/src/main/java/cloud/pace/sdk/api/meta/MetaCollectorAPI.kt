package cloud.pace.sdk.api.meta

import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.request.BaseRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.HeaderMap
import retrofit2.http.POST

object MetaCollectorAPI {

    interface MetaCollectorService {

        @POST("data")
        fun collectData(
            @HeaderMap headers: Map<String, String>,
            @retrofit2.http.Body body: MetaCollectorData
        ): Call<ResponseBody>
    }

    open class Request : BaseRequest() {

        fun collectData(
            body: MetaCollectorData,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<ResponseBody> {
            val headers = headers(true, "application/json", "application/json", additionalHeaders)

            return retrofit("${API.baseUrl}/client-data-collector/", additionalParameters, readTimeout)
                .create(MetaCollectorService::class.java)
                .collectData(headers, body)
        }
    }

    fun collectData(
        body: MetaCollectorData,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().collectData(
        body,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
