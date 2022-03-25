package cloud.pace.sdk.fueling_app.ui.payment_method

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.fueling_app.R
import cloud.pace.sdk.fueling_app.databinding.FragmentPaymentMethodsBinding
import cloud.pace.sdk.fueling_app.util.Result
import cloud.pace.sdk.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * After a gas station is selected, the [approaching at forecourt call][cloud.pace.sdk.fueling_app.data.repository.Repository.approachingAtTheForeCourt] is made,
 * which returns the user's supported and unsupported payment methods as well as information about the gas station and the pumps.
 */
@AndroidEntryPoint
class PaymentMethodsFragment : Fragment(R.layout.fragment_payment_methods) {

    private val binding by viewBinding(FragmentPaymentMethodsBinding::bind)
    private val viewModel by viewModels<PaymentMethodsViewModel>()
    private val args by navArgs<PaymentMethodsFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = args.gasStation.name
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val paymentMethodsAdapter = PaymentMethodsAdapter {
            val pumps = viewModel.pumps.value ?: emptyArray()
            findNavController().navigate(PaymentMethodsFragmentDirections.actionPaymentMethodsFragmentToPumpsFragment(args.gasStation, it, pumps))
        }

        binding.paymentMethods.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(context)
            adapter = paymentMethodsAdapter
        }

        binding.errorView.paymentMethodsButton.isVisible = true
        binding.errorView.paymentMethodsButton.setOnClickListener {
            AppKit.openPaymentApp(requireContext())
        }

        viewModel.paymentMethods.observe(viewLifecycleOwner) {
            when (it) {
                is Result.Loading -> {
                    binding.loadingView.root.isVisible = true
                    binding.content.isGone = true
                    binding.errorView.root.isGone = true
                }
                is Result.Success -> {
                    binding.loadingView.root.isGone = true

                    if (it.data.isEmpty()) {
                        binding.content.isGone = true
                        binding.errorView.text.setText(R.string.no_payment_method)
                        binding.errorView.root.isVisible = true
                    } else {
                        binding.errorView.root.isGone = true
                        binding.content.isVisible = true
                        paymentMethodsAdapter.entries = it.data
                    }
                }
                is Result.Error -> {
                    binding.loadingView.root.isGone = true
                    binding.content.isGone = true
                    binding.errorView.text.setText(R.string.generic_error)
                    binding.errorView.root.isVisible = true
                }
            }
        }
    }
}
