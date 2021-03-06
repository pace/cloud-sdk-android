/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import moe.banana.jsonapi2.HasMany
import moe.banana.jsonapi2.HasOne
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.*

class TransactionLinks {

    var receipt: Receipt? = null
    var receiptPDF: ReceiptPDF? = null

    class Receipt {

        var href: String? = null
        var meta: Meta? = null

        class Meta {

            var mimeType: MimeType? = null

            enum class MimeType(val value: String) {
                @SerializedName("image/png")
                @Json(name = "image/png")
                IMAGEPNG("image/png")
            }
        }
    }

    class ReceiptPDF {

        var href: String? = null
        var meta: Meta? = null

        class Meta {

            var mimeType: MimeType? = null

            enum class MimeType(val value: String) {
                @SerializedName("application/pdf")
                @Json(name = "application/pdf")
                APPLICATIONPDF("application/pdf")
            }
        }
    }
}
