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
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

object GetDuplicatesKMLAPI {

    interface GetDuplicatesKMLService {
        /* Duplicate Map for country (KML) */
        /* Generates a map of potential gas station duplicates (closer than 50m to eachother) for specified country. */
        @GET("datadumps/duplicatemap/{countryCode}")
        fun getDuplicatesKML(
            @HeaderMap headers: Map<String, String>,
            /* Country code in ISO 3166-1 alpha-2 format */
            @Path("countryCode") countryCode: String? = null
        ): Call<ResponseBody>
    }

    open class Request : BaseRequest() {

        fun getDuplicatesKML(
            countryCode: String? = null,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<ResponseBody> {
            val headers = headers(true, "application/json", "application/json", additionalHeaders)

            return retrofit(POIAPI.baseUrl, additionalParameters, readTimeout)
                .create(GetDuplicatesKMLService::class.java)
                .getDuplicatesKML(
                    headers,
                    countryCode
                )
        }
    }

    fun POIAPI.DataDumpsAPI.getDuplicatesKML(
        countryCode: String? = null,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().getDuplicatesKML(
        countryCode,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
