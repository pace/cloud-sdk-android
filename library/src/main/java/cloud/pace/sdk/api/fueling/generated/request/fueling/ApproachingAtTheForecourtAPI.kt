/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.fueling.generated.request.fueling

import cloud.pace.sdk.api.fueling.FuelingAPI
import cloud.pace.sdk.api.fueling.generated.model.ApproachingResponse
import cloud.pace.sdk.api.fueling.generated.model.FuelPrice
import cloud.pace.sdk.api.fueling.generated.model.GasStation
import cloud.pace.sdk.api.fueling.generated.model.GasStationNote
import cloud.pace.sdk.api.fueling.generated.model.PaymentMethod
import cloud.pace.sdk.api.fueling.generated.model.PaymentMethodKind
import cloud.pace.sdk.api.fueling.generated.model.Pump
import cloud.pace.sdk.api.fueling.generated.model.Transaction
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

object ApproachingAtTheForecourtAPI {

    interface ApproachingAtTheForecourtService {
        /* Gather information when approaching at the forecourt
 */
        /* This request will:
* Return a list of paymentMethods of the user which can be used at the
  gas station.
* Return up-to-date price information (price structure) at the gas
  station.
* Return a list of pumps available at the gas station together with the
  current status (free, inUse, readyToPay, outOfOrder). No pumps might
  be returned if the list of payment methods is empty.
The approaching is a necessary first api call for connected fueling. Without a valid approaching the [get pump](#operation/GetPump) and [wait for status change](#operation/WaitOnPumpStatusChange) calls may be answered with a `403 Forbidden` status code. An approaching is valid for one fueling only and can't be reused. If a long (not further disclosed time) has passed, the approaching is also invalidated. So if the client is receiving a `403 Forbidden` on the above mentioned calls, a new approaching has to be issued, this can be done transparent to the user.
Other than authorization, the most common error states encountered should be:
  * 404, if the gas station does not exist or ConnectedFueling is not available at this station
  * 502, if there was a communication failure with a third party (e.g. the gas station in question fails to respond). Retry is possible
  * 503, if ConnectedFueling is available, but the site is offline
 */
        @POST("gas-stations/{gasStationId}/approaching")
        fun approachingAtTheForecourt(
            @HeaderMap headers: Map<String, String>,
            /* Gas station ID */
            @Path("gasStationId") gasStationId: String,
            /* Reduces the opening hours rules. After compilation, only rules with the action open will remain in the response. */
            @Query("compile[openingHours]") compileopeningHours: Boolean? = null
        ): Call<ApproachingResponse>
    }

    open class Request : BaseRequest() {

        fun approachingAtTheForecourt(
            gasStationId: String,
            compileopeningHours: Boolean? = null,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<ApproachingResponse> {
            val resources =
                listOf(FuelPrice::class.java, GasStation::class.java, GasStationNote::class.java, PaymentMethod::class.java, PaymentMethodKind::class.java, Pump::class.java, Transaction::class.java)
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(FuelingAPI.baseUrl, additionalParameters, readTimeout, resources)
                .create(ApproachingAtTheForecourtService::class.java)
                .approachingAtTheForecourt(
                    headers,
                    gasStationId,
                    compileopeningHours
                )
        }
    }

    fun FuelingAPI.FuelingAPI.approachingAtTheForecourt(
        gasStationId: String,
        compileopeningHours: Boolean? = null,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().approachingAtTheForecourt(
        gasStationId,
        compileopeningHours,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
