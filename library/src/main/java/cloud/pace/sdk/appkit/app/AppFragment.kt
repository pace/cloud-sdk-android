package cloud.pace.sdk.appkit.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import cloud.pace.sdk.R
import cloud.pace.sdk.api.utils.InterceptorUtils
import cloud.pace.sdk.appkit.app.deeplink.DeepLinkManagementActivity
import cloud.pace.sdk.appkit.app.deeplink.DeepLinkManagementActivity.Companion.INTEGRATED
import cloud.pace.sdk.appkit.app.deeplink.DeepLinkManagementActivity.Companion.URL
import cloud.pace.sdk.databinding.FragmentAppBinding
import cloud.pace.sdk.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AppFragment : Fragment(), CloudSDKKoinComponent {

    private val binding: FragmentAppBinding by viewBinding(FragmentAppBinding::bind)
    private val viewModel: AppFragmentViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = activity?.intent?.extras?.getString(AppActivity.APP_URL)
            ?: activity?.intent?.data?.getQueryParameter(DeepLinkManagementActivity.TO)
            ?: throw RuntimeException("Missing app URL")

        binding.appWebView.init(this, InterceptorUtils.getUrlWithQueryParams(url))

        viewModel.closeEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                activity?.finish()
            }
        }

        viewModel.openUrlInNewTab.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { openURLInNewTabRequest ->
                context?.let { context ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        val intent = Intent(context, DeepLinkManagementActivity::class.java)
                            .putExtra(URL, openURLInNewTabRequest.url)
                            .putExtra(INTEGRATED, openURLInNewTabRequest.integrated)

                        when (val result = getResultFor(intent)) {
                            is Ok -> {
                                val redirectUri = result.data?.data?.toString()
                                if (redirectUri != null) {
                                    binding.appWebView.loadUrl(redirectUri)
                                } else {
                                    binding.appWebView.loadUrl(openURLInNewTabRequest.cancelUrl)
                                }
                            }
                            is Canceled -> binding.appWebView.loadUrl(openURLInNewTabRequest.cancelUrl)
                        }
                    }
                }
            }
        }
    }
}
