/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.request.auditLog

import cloud.pace.sdk.api.request.BaseRequest
import cloud.pace.sdk.api.user.UserAPI
import cloud.pace.sdk.api.user.generated.model.*
import retrofit2.Call
import retrofit2.http.*

object RecordAuditLogAPI {

    interface RecordAuditLogService {
        /* Record audit log action */
        /* Record actions of a user.
 */
        @POST("auditlogs/record")
        fun recordAuditLog(
            @HeaderMap headers: Map<String, String>,
        ): Call<AuditLogRecord>
    }

    open class Request : BaseRequest() {

        fun recordAuditLog(
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<AuditLogRecord> {
            val headers = headers(false, "application/json", "application/json", additionalHeaders)

            return retrofit(UserAPI.baseUrl, additionalParameters, readTimeout)
                .create(RecordAuditLogService::class.java)
                .recordAuditLog(
                    headers
                )
        }
    }

    fun UserAPI.AuditLogAPI.recordAuditLog(
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().recordAuditLog(
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
