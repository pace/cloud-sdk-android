/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.fueling.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import moe.banana.jsonapi2.HasMany
import moe.banana.jsonapi2.HasOne
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.*

/* Error objects provide additional information about problems encountered while performing an operation.
Errors also contain codes besides title and message which can be used for checks even if the detailed messages might change.
    * code `1000`:  generic error
    * code `1001`:  payment processing temporarily unavailable
    * code `1002`:  requested amount exceeds the authorized amount of the provided token
    * code `1003`:  implicit payment methods cannot be modified
    * code `1004`:  payment method rejected by provider
 */
class Error {

    var errors: List<Errors>? = null

    /* Error objects provide additional information about problems encountered while performing an operation.
    Errors also contain codes besides title and message which can be used for checks even if the detailed messages might change.
        * code `1000`:  generic error
        * code `1001`:  payment processing temporarily unavailable
        * code `1002`:  requested amount exceeds the authorized amount of the provided token
        * code `1003`:  implicit payment methods cannot be modified
        * code `1004`:  payment method rejected by provider
     */
    class Errors {

        /* an application-specific error code, expressed as a string value.
     */
        var code: String? = null
        /* a human-readable explanation specific to this occurrence of the problem. Like title, this field’s value can be localized.
     */
        var detail: String? = null
        var links: Links? = null
        /* a meta object containing non-standard meta-information about the error.
     */
        var meta: Map<String, Any>? = null
        /* An object containing references to the source of the error.
     */
        var source: Source? = null
        /* the HTTP status code applicable to this problem, expressed as a string value.
     */
        var status: String? = null
        /* A short, human-readable summary of the problem that SHOULD NOT change from occurrence to occurrence of the problem, except for purposes of localization.
     */
        var title: String? = null

        /* Error objects provide additional information about problems encountered while performing an operation.
        Errors also contain codes besides title and message which can be used for checks even if the detailed messages might change.
            * code `1000`:  generic error
            * code `1001`:  payment processing temporarily unavailable
            * code `1002`:  requested amount exceeds the authorized amount of the provided token
            * code `1003`:  implicit payment methods cannot be modified
            * code `1004`:  payment method rejected by provider
         */
        class Links {

            /* A link that leads to further details about this particular occurrence of the problem.
         */
            var about: String? = null
        }

        /* An object containing references to the source of the error.
         */
        class Source {

            /* A string indicating which URI query parameter caused the error.
         */
            var parameter: String? = null
            /* A JSON Pointer [RFC6901] to the associated entity in the request document [e.g. "/data" for a primary data object, or "/data/attributes/title" for a specific attribute].
         */
            var pointer: String? = null
        }
    }
}
