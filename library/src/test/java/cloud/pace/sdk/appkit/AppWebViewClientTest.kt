package cloud.pace.sdk.appkit

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import cloud.pace.sdk.appkit.app.webview.AppWebViewClient
import cloud.pace.sdk.appkit.utils.TestWebClientCallback
import cloud.pace.sdk.utils.CompletableFutureCompat
import cloud.pace.sdk.utils.random
import junit.framework.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class AppWebViewClientTest {

    private val startUrl = "https://app.test"

    @Test
    fun `intercept close request`() {
        val closeFuture = CompletableFutureCompat<AppWebViewClient.WebClientCallback.ReopenRequest?>()
        val callback = object : TestWebClientCallback() {
            override fun close(reopenRequest: AppWebViewClient.WebClientCallback.ReopenRequest?) {
                closeFuture.complete(reopenRequest)
            }

            override fun onLoadingChanged(isLoading: Boolean) {}
        }

        val webViewClient = AppWebViewClient(startUrl, callback)
        val request = mock(WebResourceRequest::class.java)
        val uri = mock(Uri::class.java)
        val webView = mock(WebView::class.java)
        `when`(uri.toString()).thenReturn("pacepwasdk://action/close")
        `when`(request.url).thenReturn(uri)
        `when`(webView.url).thenReturn("https://www.pace.car")

        webViewClient.shouldOverrideUrlLoading(webView, request)

        val reopenRequest = closeFuture.get()
        assertNull(reopenRequest)
    }

    @Test
    fun `intercept minimize request`() {
        val reopenUrl = "https://app.test.reopen"
        val state = String.random(8)
        val reopenTitle = "Continue app"
        val reopenSubtitle = "Open to continue"

        val closeFuture = CompletableFutureCompat<AppWebViewClient.WebClientCallback.ReopenRequest?>()
        val callback = object : TestWebClientCallback() {
            override fun close(reopenRequest: AppWebViewClient.WebClientCallback.ReopenRequest?) {
                closeFuture.complete(reopenRequest)
            }

            override fun onLoadingChanged(isLoading: Boolean) {}
        }

        val webViewClient = AppWebViewClient(startUrl, callback)
        val request = mock(WebResourceRequest::class.java)
        val uri = mock(Uri::class.java)
        val webView = mock(WebView::class.java)
        `when`(uri.toString()).thenReturn("pacepwasdk://action/close?reopen_url=$reopenUrl&state=$state&reopen_title=$reopenTitle&reopen_subtitle=$reopenSubtitle")
        `when`(uri.getQueryParameter(AppWebViewClient.REOPEN_URL)).thenReturn(reopenUrl)
        `when`(uri.getQueryParameter(AppWebViewClient.STATE)).thenReturn(state)
        `when`(uri.getQueryParameter(AppWebViewClient.REOPEN_TITLE)).thenReturn(reopenTitle)
        `when`(uri.getQueryParameter(AppWebViewClient.REOPEN_SUBTITLE)).thenReturn(reopenSubtitle)
        `when`(request.url).thenReturn(uri)
        `when`(webView.url).thenReturn("https://www.pace.car")

        webViewClient.shouldOverrideUrlLoading(webView, request)

        val reopenRequest = closeFuture.get()
        assertNotNull(reopenRequest)
        assertEquals(reopenUrl, reopenRequest?.reopenUrl)
        assertEquals(state, reopenRequest?.state)
        assertEquals(reopenTitle, reopenRequest?.reopenTitle)
        assertEquals(reopenSubtitle, reopenRequest?.reopenSubtitle)
    }

    @Test
    fun `intercept network error`() {
        val errorFuture = CompletableFutureCompat<Boolean>()
        val httpErrorFuture = CompletableFutureCompat<Boolean>()
        val callback = object : TestWebClientCallback() {
            override fun onSwitchErrorState(isError: Boolean, isHttpError: Boolean) {
                errorFuture.complete(isError)
                httpErrorFuture.complete(isHttpError)
            }

            override fun onLoadingChanged(isLoading: Boolean) {}
        }

        val webViewClient = AppWebViewClient(startUrl, callback)
        createError(webViewClient, false)

        assertTrue(errorFuture.get())
        assertFalse(httpErrorFuture.get())
    }

    @Test
    fun `intercept http error`() {
        val errorFuture = CompletableFutureCompat<Boolean>()
        val httpErrorFuture = CompletableFutureCompat<Boolean>()
        val callback = object : TestWebClientCallback() {
            override fun onSwitchErrorState(isError: Boolean, isHttpError: Boolean) {
                errorFuture.complete(isError)
                httpErrorFuture.complete(isHttpError)
            }

            override fun onLoadingChanged(isLoading: Boolean) {}
        }

        val webViewClient = AppWebViewClient(startUrl, callback)
        createError(webViewClient, true)

        assertTrue(errorFuture.get())
        assertTrue(httpErrorFuture.get())
    }

    @Test
    fun `network error changed to http error`() {
        var catchCallback = false
        val errorFuture = CompletableFutureCompat<Boolean>()
        val httpErrorFuture = CompletableFutureCompat<Boolean>()
        val callback = object : TestWebClientCallback() {
            override fun onSwitchErrorState(isError: Boolean, isHttpError: Boolean) {
                if (!catchCallback) return
                errorFuture.complete(isError)
                httpErrorFuture.complete(isHttpError)
            }

            override fun onLoadingChanged(isLoading: Boolean) {}
        }

        val webViewClient = AppWebViewClient(startUrl, callback)

        createError(webViewClient, false)
        catchCallback = true
        createError(webViewClient, true)

        assertTrue(errorFuture.get(2, TimeUnit.SECONDS))
        assertTrue(httpErrorFuture.get(2, TimeUnit.SECONDS))
    }

    @Test
    fun `http error changed to network error`() {
        var catchCallback = false
        val errorFuture = CompletableFutureCompat<Boolean>()
        val httpErrorFuture = CompletableFutureCompat<Boolean>()
        val callback = object : TestWebClientCallback() {
            override fun onSwitchErrorState(isError: Boolean, isHttpError: Boolean) {
                if (!catchCallback) return
                errorFuture.complete(isError)
                httpErrorFuture.complete(isHttpError)
            }

            override fun onLoadingChanged(isLoading: Boolean) {}
        }

        val webViewClient = AppWebViewClient(startUrl, callback)

        createError(webViewClient, true)
        catchCallback = true
        createError(webViewClient, false)

        assertTrue(errorFuture.get(2, TimeUnit.SECONDS))
        assertFalse(httpErrorFuture.get(2, TimeUnit.SECONDS))
    }

    @Test
    fun `page loaded successfully after network error`() {
        var catchCallback = false
        val errorFuture = CompletableFutureCompat<Boolean>()
        val httpErrorFuture = CompletableFutureCompat<Boolean>()
        val callback = object : TestWebClientCallback() {
            override fun onSwitchErrorState(isError: Boolean, isHttpError: Boolean) {
                if (!catchCallback) return
                errorFuture.complete(isError)
                httpErrorFuture.complete(isHttpError)
            }

            override fun onLoadingChanged(isLoading: Boolean) {}
        }

        val webViewClient = AppWebViewClient(startUrl, callback)

        createError(webViewClient, false)
        catchCallback = true
        val mockWebView = mock(WebView::class.java)
        webViewClient.onPageStarted(mockWebView, startUrl, mock(Bitmap::class.java))
        webViewClient.chromeClient.onProgressChanged(mockWebView, 100)

        assertFalse(errorFuture.get(2, TimeUnit.SECONDS))
        assertFalse(httpErrorFuture.get(2, TimeUnit.SECONDS))
    }

    @Test
    fun `page loaded successfully after http error`() {
        var catchCallback = false
        val errorFuture = CompletableFutureCompat<Boolean>()
        val httpErrorFuture = CompletableFutureCompat<Boolean>()
        val callback = object : TestWebClientCallback() {
            override fun onSwitchErrorState(isError: Boolean, isHttpError: Boolean) {
                if (!catchCallback) return
                errorFuture.complete(isError)
                httpErrorFuture.complete(isHttpError)
            }

            override fun onLoadingChanged(isLoading: Boolean) {}
        }

        val webViewClient = AppWebViewClient(startUrl, callback)

        createError(webViewClient, true)
        catchCallback = true
        val mockWebView = mock(WebView::class.java)
        webViewClient.onPageStarted(mockWebView, startUrl, mock(Bitmap::class.java))
        webViewClient.chromeClient.onProgressChanged(mockWebView, 100)

        assertFalse(errorFuture.get(2, TimeUnit.SECONDS))
        assertFalse(httpErrorFuture.get(2, TimeUnit.SECONDS))
    }

    private fun createError(webViewClient: AppWebViewClient, isHttpError: Boolean) {
        val mockWebRequest = mock(WebResourceRequest::class.java)
        val mockUri = mock(Uri::class.java)
        `when`(mockWebRequest.url).thenReturn(mockUri)
        `when`(mockUri.toString()).thenReturn(startUrl)
        if (isHttpError) {
            webViewClient.onReceivedHttpError(mock(WebView::class.java), mockWebRequest, mock(WebResourceResponse::class.java))
        } else {
            webViewClient.onReceivedError(mock(WebView::class.java), mockWebRequest, mock(WebResourceError::class.java))
        }
    }
}
