/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.fueling.generated.request.fueling

import cloud.pace.sdk.api.fueling.FuelingAPI
import cloud.pace.sdk.api.fueling.generated.model.ProcessPaymentResponse
import cloud.pace.sdk.api.fueling.generated.model.TransactionRequest
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path

object ProcessPaymentAPI {

    interface ProcessPaymentService {
        /* Pre Auth or Post Pay */
        /* This call supports two different flows. The *Pre Auth* flow and the *Post Pay* flow. This call will notify the user via email with a payment receipt if transaction is finished successfully. Only use after approaching, otherwise returns `403 Forbidden`.
### Pre Auth
This flow is used if a pump is having the status `locked`. A `locked` pump requires a *Pre Auth* to unlock. Only after this *Pre Auth* the pump and can be used by the user
* `carFuelType` may be passed to only unlock a certain nozzle of the pump.
  Not all pumps support this feature, and some require it. It is advised to
  always pass the desired fuel type.
### Post Pay
You can optionally provide:
* `priceIncludingVAT` and `currency` in the request body to check if the price the user has seen is still correct.
  If the values don't match, the status `409 Conflict` is returned.
* `carFuelType` may be provided but has no effect.
* The token will still be available for use in case the payment fails. It may need to be deleted by the caller
### Unattended Payment
As an additional feature, the caller can - in case of Post Pay - opt for unattended payment. In summary, this means that when given a token with a certain authorized amount, the service will watch over the status of the fueling process and finish the transaction in the background, with no further user interaction required. This will only work if the amount fueled does not exceed the amount authorized in the token. Otherwise, an error response will be sent to the callback URL
* `unattendedPayment` in the request body designates whether this should be done or not. Note that this is only
  possible if a valid token has been provided
* `callbackURL` is an optional URL that will be called with what would usually be the response to the final
  `ProcessPayment` call after the process has finished
* The token will still be available for use in case the payment fails. It may need to be deleted by the caller
* This process is only available if the pump is in status `free`. Otherwise, `422` is returned.
* The backend will wait for a grace period of 20 seconds after this call was sent before starting the process
 */
        @POST("gas-stations/{gasStationId}/transactions")
        fun processPayment(
            @HeaderMap headers: Map<String, String>,
            /* Gas station ID */
            @Path("gasStationId") gasStationId: String,
            @retrofit2.http.Body body: TransactionRequest
        ): Call<ProcessPaymentResponse>
    }

    open class Request : BaseRequest() {

        fun processPayment(
            gasStationId: String,
            body: TransactionRequest,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<ProcessPaymentResponse> {
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(FuelingAPI.baseUrl, additionalParameters, readTimeout)
                .create(ProcessPaymentService::class.java)
                .processPayment(
                    headers,
                    gasStationId,
                    body
                )
        }
    }

    fun FuelingAPI.FuelingAPI.processPayment(
        gasStationId: String,
        body: TransactionRequest,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().processPayment(
        gasStationId,
        body,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
