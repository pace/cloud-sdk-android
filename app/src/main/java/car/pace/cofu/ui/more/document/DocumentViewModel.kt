package car.pace.cofu.ui.more.document

import android.content.Context
import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import car.pace.cofu.data.DocumentRepository
import car.pace.cofu.ui.component.AccompanistWebViewClient
import car.pace.cofu.ui.consent.Consent
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.MAILTO_SCHEME
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.SMS_SCHEME
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.TEL_SCHEME
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    fun getUrl(document: Consent.Document): String {
        val language = documentRepository.getLanguage(document)
        return document.getUrl(language)
    }

    fun createClient(context: Context) = object : AccompanistWebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val uri = request?.url
            val action = when (uri?.scheme) {
                MAILTO_SCHEME, SMS_SCHEME -> Intent.ACTION_SENDTO
                TEL_SCHEME -> Intent.ACTION_DIAL
                else -> Intent.ACTION_VIEW
            }
            context.startActivity(Intent(action, uri))

            return true
        }
    }
}
