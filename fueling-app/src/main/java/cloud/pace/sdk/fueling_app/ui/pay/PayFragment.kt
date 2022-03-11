package cloud.pace.sdk.fueling_app.ui.pay

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cloud.pace.sdk.api.fueling.generated.model.PumpResponse
import cloud.pace.sdk.appkit.utils.BiometricUtils
import cloud.pace.sdk.fueling_app.R
import cloud.pace.sdk.fueling_app.databinding.FragmentPayBinding
import cloud.pace.sdk.fueling_app.util.ProductDeniedException
import cloud.pace.sdk.fueling_app.util.Result
import cloud.pace.sdk.fueling_app.util.WrongInputException
import cloud.pace.sdk.fueling_app.util.localizedKind
import cloud.pace.sdk.utils.dp
import cloud.pace.sdk.utils.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.*

/**
 * This fragment shows an overview of the fueling data e.g. total amount, price per liter or fuel amount that the user has to pay.
 */
@AndroidEntryPoint
class PayFragment : Fragment(R.layout.fragment_pay) {

    private val binding by viewBinding(FragmentPayBinding::bind)
    private val viewModel by viewModels<PayViewModel>()
    private val args by navArgs<PayFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = args.gasStation.name
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.pumpNumber.text = getString(R.string.pre_auth_amount_pump_number, args.pumpResponse.identifier)
        binding.paymentMethod.text = args.paymentMethod.localizedKind(requireContext()) + "\n" + (args.paymentMethod.alias ?: args.paymentMethod.identificationString.orEmpty())
        binding.location.text = args.gasStation.name + "\n" + args.gasStation.address
        binding.recipient.text = args.paymentMethod.merchantName

        val currency = args.gasStation.currency
        binding.amount.text = args.pumpResponse.priceIncludingVAT?.let { price ->
            NumberFormat.getCurrencyInstance().apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
                currency?.let { this.currency = Currency.getInstance(it) }
            }.format(price)
        }

        if (args.pumpResponse.fuelingProcess == PumpResponse.FuelingProcess.POSTPAY) {
            val fuelAmount = args.pumpResponse.fuelAmount?.let {
                NumberFormat.getInstance().apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 2
                }.format(it)
            }
            binding.productName.text = args.pumpResponse.productName
            binding.fuelAmount.text = getString(R.string.pay_fuel_amount, fuelAmount)

            val pricePerUnit = args.pumpResponse.pricePerUnit?.let { price ->
                NumberFormat.getCurrencyInstance().apply {
                    minimumFractionDigits = 3
                    maximumFractionDigits = 3
                    currency?.let { this.currency = Currency.getInstance(it) }
                }.format(price)
            }
            binding.pricePerUnit.text = getString(R.string.pay_price_per_unit_value, pricePerUnit)
        } else {
            binding.postPayOnlyValues.isGone = true
        }

        binding.payButton.setOnClickListener {
            it.isEnabled = false
            viewModel.processPayment()
        }

        viewModel.paymentResult.observe(viewLifecycleOwner) {
            when (it) {
                is Result.Loading -> {
                    binding.loadingView.root.isVisible = true
                    binding.content.isGone = true
                    binding.errorView.root.isGone = true
                }
                is Result.Success -> {
                    if (it.data.first == PumpResponse.FuelingProcess.POSTPAY) {
                        findNavController().navigate(PayFragmentDirections.actionPayFragmentToSummaryFragment(it.data.second))
                    } else {
                        findNavController().navigate(PayFragmentDirections.actionPayFragmentToStatusFragment(args.gasStation, args.pump, args.paymentMethod))
                    }
                }
                is Result.Error -> {
                    binding.loadingView.root.isGone = true
                    binding.content.isGone = true
                    binding.errorView.root.isVisible = true

                    when (it.exception) {
                        is ProductDeniedException -> binding.errorView.text.setText(R.string.pay_rejected)
                        is WrongInputException -> binding.errorView.text.setText(R.string.pay_invalid_input)
                        else -> findNavController().navigate(PayFragmentDirections.actionPayFragmentToSummaryFragment(null))
                    }
                }
            }
        }

        viewModel.biometricRequest.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { callback ->
                BiometricUtils.requestAuthentication(this, getString(callback.title), onSuccess = callback.onSuccess, onFailure = callback.onFailure)
            }
        }

        viewModel.showDialog.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                binding.loadingView.root.isVisible = result is Result.Loading
                binding.content.isVisible = result is Result.Success
                binding.errorView.root.isVisible = result is Result.Error

                if (result is Result.Success) {
                    val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        marginStart = 20.dp
                        marginEnd = 20.dp
                    }
                    val editText = EditText(requireContext()).apply {
                        isSingleLine = true
                        layoutParams = params
                        inputType = if (result.data == PayViewModel.DialogType.PASSWORD) {
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        } else {
                            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
                        }
                    }
                    val container = FrameLayout(requireContext()).apply {
                        addView(editText)
                    }

                    val message = when (result.data) {
                        PayViewModel.DialogType.PIN -> R.string.pay_enter_pin
                        PayViewModel.DialogType.PASSWORD -> R.string.pay_enter_password
                        PayViewModel.DialogType.MAIL -> R.string.pay_enter_mail_otp
                    }

                    MaterialAlertDialogBuilder(requireContext())
                        .setIcon(R.drawable.ic_lock)
                        .setTitle(R.string.pay_button)
                        .setMessage(message)
                        .setView(container)
                        .setNeutralButton(R.string.common_done) { dialog, _ ->
                            viewModel.setDialogInput(editText.text?.toString(), result.data)
                            dialog.dismiss()
                        }
                        .setOnCancelListener {
                            binding.payButton.isEnabled = true
                        }
                        .show()
                }
            }
        }
    }
}
