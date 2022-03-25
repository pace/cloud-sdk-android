package cloud.pace.sdk.fueling_app.ui.status

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cloud.pace.sdk.fueling_app.R
import cloud.pace.sdk.fueling_app.data.model.PostPay
import cloud.pace.sdk.fueling_app.data.model.PreAuth
import cloud.pace.sdk.fueling_app.databinding.FragmentStatusBinding
import cloud.pace.sdk.fueling_app.util.Result
import cloud.pace.sdk.utils.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

/**
 * This fragment is displayed during the fueling process, e.g. when the pump is in use.
 * The status is updated with the [wait on pump status change call][cloud.pace.sdk.fueling_app.data.repository.Repository].
 * There are two different fueling processes:
 * - Post pay: First you fuel and then you pay
 * - Pre auth: First you authorize your maximum amount you want to spend for fueling and then you can fuel up to this amount
 */
@AndroidEntryPoint
class StatusFragment : Fragment(R.layout.fragment_status) {

    private val binding by viewBinding(FragmentStatusBinding::bind)
    private val viewModel by viewModels<StatusViewModel>()
    private val args by navArgs<StatusFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            val pumpStatus = viewModel.pumpStatus.value
            if (pumpStatus is Result.Success && pumpStatus.data == PreAuth.Free) {
                cancelPreAuth()
            } else {
                findNavController().popBackStack()
            }
        }

        binding.toolbar.title = args.gasStation.name
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.pumpNumber.text = getString(R.string.pre_auth_amount_pump_number, args.pump.identifier)

        viewModel.pumpStatus.observe(viewLifecycleOwner) {
            binding.loadingView.root.isVisible = it is Result.Loading
            binding.content.isVisible = it is Result.Success
            binding.errorView.root.isVisible = it is Result.Error

            if (it is Result.Success) {
                binding.cancelPreAuthButton.isVisible = it.data == PreAuth.Free || it.data is PreAuth.Canceled

                when (it.data) {
                    PostPay.Free, is PreAuth.Free -> {
                        binding.cancelPreAuthButton.setOnClickListener {
                            cancelPreAuth()
                        }
                        binding.image.setImageResource(R.drawable.ic_pump_free)
                        binding.title.setText(R.string.pump_status_free_title)
                        binding.description.setText(R.string.pump_status_free_description)
                    }
                    PostPay.InUse, PreAuth.InUse -> {
                        binding.image.setImageResource(R.drawable.ic_pump_in_use)
                        binding.title.setText(R.string.pump_status_in_use_title)
                        binding.description.setText(R.string.pump_status_in_use_description)
                    }
                    is PostPay.ReadyToPay -> findNavController().navigate(
                        StatusFragmentDirections.actionStatusFragmentToPayFragment(
                            args.gasStation,
                            args.paymentMethod,
                            args.pump,
                            it.data.pumpResponse
                        )
                    )
                    is PreAuth.Done -> findNavController().navigate(StatusFragmentDirections.actionStatusFragmentToSummaryFragment(it.data.transactionId))
                    PreAuth.InTransaction -> {
                        binding.image.setImageResource(R.drawable.ic_pump)
                        binding.title.setText(R.string.pump_status_in_transaction_title)
                        binding.description.setText(R.string.pump_status_in_transaction_description)
                    }
                    PreAuth.Locked -> findNavController().popBackStack()
                    PostPay.OutOfOrder, PreAuth.OutOfOrder -> {
                        binding.image.setImageResource(R.drawable.ic_pump_error)
                        binding.title.setText(R.string.pump_status_out_of_order_title)
                        binding.description.setText(R.string.pump_status_out_of_order_description)
                    }
                    is PreAuth.Canceled -> {
                        binding.cancelPreAuthButton.setOnClickListener {
                            findNavController().popBackStack()
                        }
                        binding.image.setImageResource(if (it.data.successful) R.drawable.ic_pump else R.drawable.ic_pump_error)
                        binding.title.setText(if (it.data.successful) R.string.pre_auth_canceled_successful_title else R.string.pre_auth_canceled_failed_title)
                        binding.description.setText(if (it.data.successful) R.string.pre_auth_canceled_successful_description else R.string.pre_auth_canceled_failed_description)
                        binding.cancelPreAuthButton.setText(R.string.pre_auth_canceled_button)
                    }
                }
            }
        }
    }

    private fun cancelPreAuth() {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_cancel)
            .setTitle(R.string.cancel_pre_auth)
            .setMessage(R.string.pre_auth_dialog_message)
            .setPositiveButton(R.string.common_yes) { dialog, _ ->
                viewModel.cancelPreAuth()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.common_no, null)
            .show()
    }
}
