package car.pace.cofu.ui.webview

import android.os.Bundle
import androidx.navigation.fragment.navArgs
import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.mvvm.BaseFragment
import car.pace.cofu.databinding.FragmentWebviewBinding
import dagger.hilt.android.AndroidEntryPoint

// Workaround for https://github.com/google/dagger/issues/1904
abstract class BaseWebViewFragment :
    BaseFragment<FragmentWebviewBinding, WebViewViewModel>(
        R.layout.fragment_webview,
        WebViewViewModel::class
    )

@AndroidEntryPoint
class WebViewFragment : BaseWebViewFragment() {

    private val args: WebViewFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.url.set(args.url)
    }

    private val webView get() = getBinding<FragmentWebviewBinding>()?.webview

    override fun onConsumeBackPress(): Boolean {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
            return true
        }
        return super.onConsumeBackPress()
    }

    override fun onHandleFragmentEvent(event: FragmentEvent) {
        when(event) {
            is WebViewViewModel.ReloadWebView -> webView?.reload()
            else -> super.onHandleFragmentEvent(event)
        }
    }
}

