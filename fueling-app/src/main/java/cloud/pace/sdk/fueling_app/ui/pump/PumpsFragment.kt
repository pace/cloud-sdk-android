package cloud.pace.sdk.fueling_app.ui.pump

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import cloud.pace.sdk.fueling_app.R
import cloud.pace.sdk.fueling_app.databinding.FragmentPumpsBinding
import cloud.pace.sdk.fueling_app.util.Result
import cloud.pace.sdk.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * After a payment method has been selected, one of the pumps received from the [approaching at forecourt call][cloud.pace.sdk.fueling_app.data.repository.Repository.approachingAtTheForeCourt]
 * (in [PaymentMethodsViewModel][cloud.pace.sdk.fueling_app.ui.payment_method.PaymentMethodsViewModel]) must be selected here.
 */
@AndroidEntryPoint
class PumpsFragment : Fragment(R.layout.fragment_pumps) {

    private val binding by viewBinding(FragmentPumpsBinding::bind)
    private val viewModel by viewModels<PumpsViewModel>()
    private val args by navArgs<PumpsFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = args.gasStation.name
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val pumpsAdapter = PumpsAdapter(args.pumps) {
            viewModel.selectedPump.value = it
        }

        binding.pumps.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 2)
            adapter = pumpsAdapter
        }

        viewModel.navigateTo.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                binding.loadingView.root.isVisible = result is Result.Loading
                binding.errorView.root.isVisible = result is Result.Error

                if (result is Result.Success) {
                    findNavController().navigate(result.data)
                } else {
                    binding.content.isGone = true
                }
            }
        }
    }
}
