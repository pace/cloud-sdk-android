package cloud.pace.sdk.fueling_app.ui.amount

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cloud.pace.sdk.fueling_app.R
import cloud.pace.sdk.fueling_app.databinding.FragmentAmountBinding
import cloud.pace.sdk.fueling_app.util.Constants.DEFAULT_PRE_AUTH_AMOUNT
import cloud.pace.sdk.fueling_app.util.Constants.DEFAULT_PRE_AUTH_CURRENCY
import cloud.pace.sdk.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

/**
 * This fragment is displayed only if the fueling process is pre auth. Here the user can set his maximum amount he wants to spend for fueling.
 */
@AndroidEntryPoint
class AmountFragment : Fragment(R.layout.fragment_amount) {

    private val binding by viewBinding(FragmentAmountBinding::bind)
    private val args by navArgs<AmountFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = args.gasStation.name
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.pumpNumber.text = getString(R.string.pre_auth_amount_pump_number, args.pumpResponse.identifier)

        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) {
                binding.continueButton.isEnabled = isInputValid(binding.amount.text?.toString())
            } else {
                binding.amount.text?.clear()
                binding.continueButton.isEnabled = true
            }
        }

        binding.amount.addTextChangedListener(
            onTextChanged = { text, _, _, _ ->
                binding.continueButton.isEnabled = isInputValid(text?.toString()) || binding.chipGroup.checkedChipIds.isNotEmpty()
            },
            afterTextChanged = {
                if (isInputValid(it?.toString())) {
                    binding.chipGroup.clearCheck()
                } else {
                    it?.clear()
                }
            }
        )

        binding.currency.text = args.gasStation.currency?.let { Currency.getInstance(it).symbol } ?: DEFAULT_PRE_AUTH_CURRENCY

        binding.continueButton.setOnClickListener {
            it.isEnabled = false

            val amount = when (binding.chipGroup.checkedChipId) {
                R.id.chip_25 -> 25.0
                R.id.chip_50 -> 50.0
                R.id.chip_100 -> 100.0
                R.id.chip_150 -> 150.0
                else -> binding.amount.text?.toString()?.toDoubleOrNull() ?: DEFAULT_PRE_AUTH_AMOUNT
            }

            findNavController().navigate(
                AmountFragmentDirections.actionAmountFragmentToPayFragment(
                    args.gasStation,
                    args.paymentMethod,
                    args.pump,
                    args.pumpResponse.apply { priceIncludingVAT = amount })
            )
        }
    }

    private fun isInputValid(input: String?): Boolean {
        val doubleValue = input?.toDoubleOrNull()
        return doubleValue != null && doubleValue > 0.0
    }
}
