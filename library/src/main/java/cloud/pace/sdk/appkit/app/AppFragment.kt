package cloud.pace.sdk.appkit.app

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import cloud.pace.sdk.R
import cloud.pace.sdk.appkit.app.webview.AppWebViewClient
import cloud.pace.sdk.utils.AppKitKoinComponent
import kotlinx.android.synthetic.main.fragment_app.*
import org.koin.android.viewmodel.ext.android.viewModel

class AppFragment : Fragment(), AppKitKoinComponent {

    private val viewModel: AppFragmentViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = activity?.intent?.extras?.getString(AppActivity.APP_URL)
            ?: activity?.intent?.data?.getQueryParameter(AppWebViewClient.TO)
            ?: throw RuntimeException("Missing app URL")

        val autoClose = activity?.intent?.extras?.getBoolean(AppActivity.AUTO_CLOSE) ?: true

        appWebView.loadApp(this, url)

        viewModel.closeEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { force ->
                if (activity is AppActivity && force || autoClose) {
                    activity?.finish()
                }
            }
        }

        viewModel.openUrlInNewTab.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { url ->
                context?.let { context ->
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    customTabsIntent.launchUrl(context, Uri.parse(url))
                }
            }
        }

        viewModel.redirectEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                appWebView.loadApp(this, it)
            }
        }
    }
}
