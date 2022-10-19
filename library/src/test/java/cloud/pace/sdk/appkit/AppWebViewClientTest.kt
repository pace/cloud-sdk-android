package cloud.pace.sdk.appkit

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import cloud.pace.sdk.appkit.app.webview.AppWebViewClient
import cloud.pace.sdk.appkit.utils.TestWebClientCallback
import cloud.pace.sdk.utils.CompletableFutureCompat
import io.mockk.mockk
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.concurrent.TimeUnit

class AppWebViewClientTest {

    private val startUrl = "https://app.test"
    private val context = mockk<Context>(relaxed = true)

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

        val webViewClient = AppWebViewClient(startUrl, callback, context)
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

        val webViewClient = AppWebViewClient(startUrl, callback, context)
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

        val webViewClient = AppWebViewClient(startUrl, callback, context)

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

        val webViewClient = AppWebViewClient(startUrl, callback, context)

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

        val webViewClient = AppWebViewClient(startUrl, callback, context)

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

        val webViewClient = AppWebViewClient(startUrl, callback, context)

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
        `when`(mockWebRequest.isForMainFrame).thenReturn(true)
        `when`(mockUri.toString()).thenReturn(startUrl)
        if (isHttpError) {
            webViewClient.onReceivedHttpError(mock(WebView::class.java), mockWebRequest, mock(WebResourceResponse::class.java))
        } else {
            webViewClient.onReceivedError(mock(WebView::class.java), mockWebRequest, mock(WebResourceError::class.java))
        }
    }
}
