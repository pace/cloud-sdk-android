package car.pace.cofu.util.extension

import android.net.Uri
import cloud.pace.sdk.appkit.app.webview.AppWebViewClient
import cloud.pace.sdk.utils.URL

val URL.paymentMethodCreate: String
    get() {
        return Uri.parse(payment)
            .buildUpon()
            .appendPath("payment-create")
            .appendQueryParameter("redirect_uri", AppWebViewClient.CLOSE_URI)
            .build()
            .toString()
    }

fun URL.paymentMethod(id: String?): String {
    id ?: return payment

    return Uri.parse(payment)
        .buildUpon()
        .appendPath("payment-method")
        .appendPath(id)
        .appendQueryParameter("redirect_uri", AppWebViewClient.CLOSE_URI)
        .build()
        .toString()
}
