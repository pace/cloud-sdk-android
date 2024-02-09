package car.pace.cofu.ui.more.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import car.pace.cofu.ui.component.AccompanistWebViewClient
import car.pace.cofu.ui.component.TextTopBar
import car.pace.cofu.ui.component.WebView
import car.pace.cofu.ui.component.rememberWebViewState
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.Constants
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.MAILTO_SCHEME
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.SMS_SCHEME
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.TEL_SCHEME

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    url: String,
    onNavigateUp: () -> Unit
) {
    Column {
        val context = LocalContext.current
        val client = remember {
            object : AccompanistWebViewClient() {
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

        TextTopBar(
            onNavigateUp = onNavigateUp
        )

        WebView(
            state = rememberWebViewState(url),
            modifier = Modifier.fillMaxSize(),
            onCreated = { it.settings.javaScriptEnabled = true },
            client = client
        )
    }
}

@Preview
@Composable
fun WebViewScreenPreview() {
    AppTheme {
        WebViewScreen(
            url = Constants.IMPRINT_URI,
            onNavigateUp = {}
        )
    }
}
