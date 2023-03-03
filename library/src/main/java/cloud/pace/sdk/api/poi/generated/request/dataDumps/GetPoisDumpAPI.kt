/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.request.dataDumps

import cloud.pace.sdk.api.poi.POIAPI
import cloud.pace.sdk.api.poi.generated.model.*
import cloud.pace.sdk.api.request.BaseRequest
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

object GetPoisDumpAPI {

    interface GetPoisDumpService {
        /* Create a full POI dump
 */
        /* Dump all POI data in XLSX format, along with full amenities.
 */
        @GET("datadumps/pois")
        fun getPoisDump(
            @HeaderMap headers: Map<String, String>,
            @Header("Accept") accept: Accept
        ): Call<ResponseBody>
    }

    /* Dump all POI data in XLSX format, along with full amenities.
     */
    enum class Accept(val value: String) {
        @SerializedName("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        @Json(name = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        APPLICATIONVNDOPENXMLFORMATSOFFICEDOCUMENTSPREADSHEETMLSHEET("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    }

    open class Request : BaseRequest() {

        fun getPoisDump(
            accept: Accept,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<ResponseBody> {
            val headers = headers(true, "application/json", "application/json", additionalHeaders)

            return retrofit(POIAPI.baseUrl, additionalParameters, readTimeout)
                .create(GetPoisDumpService::class.java)
                .getPoisDump(
                    headers,
                    accept
                )
        }
    }

    fun POIAPI.DataDumpsAPI.getPoisDump(
        accept: Accept,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().getPoisDump(
        accept,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
