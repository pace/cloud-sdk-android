package car.pace.cofu.ui.more.legal

import android.content.Context
import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import car.pace.cofu.data.LegalRepository
import car.pace.cofu.ui.component.AccompanistWebViewClient
import car.pace.cofu.ui.more.legal.update.LegalDocument
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.MAILTO_SCHEME
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.SMS_SCHEME
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.TEL_SCHEME
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LegalDocumentViewModel @Inject constructor(
    private val legalRepository: LegalRepository
) : ViewModel() {

    fun getUrl(legalDocument: LegalDocument): String {
        val language = legalRepository.getLanguage(legalDocument)
        return legalDocument.getUrl(language)
    }

    fun createClient(context: Context) = object : AccompanistWebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val uri = request?.url
            if (uri?.scheme == MAILTO_SCHEME || uri?.scheme == SMS_SCHEME) {
                context.startActivity(Intent(Intent.ACTION_SENDTO, uri))
                return true
            } else if (uri?.scheme == TEL_SCHEME) {
                context.startActivity(Intent(Intent.ACTION_DIAL, uri))
                return true
            }

            return false
        }
    }
}
