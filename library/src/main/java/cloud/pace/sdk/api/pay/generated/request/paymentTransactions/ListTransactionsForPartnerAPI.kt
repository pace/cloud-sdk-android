/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.paymentTransactions

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.pay.generated.model.*
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.*
import java.util.Date

object ListTransactionsForPartnerAPI {

    interface ListTransactionsForPartnerService {
        /* List transactions for the given partner */
        /* List all transactions for the given partner within the given time frame.
 */
        @GET("transactions/contracts/partners/{partner}")
        fun listTransactionsForPartner(
            @HeaderMap headers: Map<String, String>,
            /* Partner name for which the transactions shall be returned. */
            @Path("partner") partner: String,
            /* Number of the page that should be returned (sometimes referred to as "offset"). Page `0` is the first page. */
            @Query("page[number]") pagenumber: Int? = null,
            /* Page size of the currently returned page (sometimes referred to as "limit"). */
            @Query("page[size]") pagesize: Int? = null,
            /* Start date of the period for which transactions should be returned. */
            @Query("date_from") dateFrom: Date,
            /* End date of the period for which transactions should be returned. */
            @Query("date_to") dateTo: Date
        ): Call<TransactionsShort>
    }

    open class Request : BaseRequest() {

        fun listTransactionsForPartner(
            partner: String,
            pagenumber: Int? = null,
            pagesize: Int? = null,
            dateFrom: Date,
            dateTo: Date,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<TransactionsShort> {
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(PayAPI.baseUrl, additionalParameters, readTimeout)
                .create(ListTransactionsForPartnerService::class.java)
                .listTransactionsForPartner(
                    headers,
                    partner,
                    pagenumber,
                    pagesize,
                    dateFrom,
                    dateTo
                )
        }
    }

    fun PayAPI.PaymentTransactionsAPI.listTransactionsForPartner(
        partner: String,
        pagenumber: Int? = null,
        pagesize: Int? = null,
        dateFrom: Date,
        dateTo: Date,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().listTransactionsForPartner(
        partner,
        pagenumber,
        pagesize,
        dateFrom,
        dateTo,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
