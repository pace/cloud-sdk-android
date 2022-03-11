package cloud.pace.sdk.fueling_app.ui.summary

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cloud.pace.sdk.fueling_app.R
import cloud.pace.sdk.fueling_app.databinding.FragmentSummaryBinding
import cloud.pace.sdk.fueling_app.util.Result
import cloud.pace.sdk.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * This fragment is the last fragment and shows whether the payment was successful or not. If the payment was successful, the receipt can be opened in the system's image viewer.
 */
@AndroidEntryPoint
class SummaryFragment : Fragment(R.layout.fragment_summary) {

    private val binding by viewBinding(FragmentSummaryBinding::bind)
    private val viewModel by viewModels<SummaryViewModel>()
    private val args by navArgs<SummaryFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val transactionId = args.transactionId
        if (transactionId != null) {
            // Fueling process succeeded
            binding.receiptButton.setOnClickListener {
                viewModel.transactionId.value = transactionId
            }
        } else {
            // Fueling process failed
            binding.image.setImageResource(R.drawable.ic_pump_error)
            binding.title.setText(R.string.summary_failure_title)
            binding.description.setText(R.string.summary_failure_description)
            binding.note.isGone = true
            binding.receiptButton.isGone = true
        }

        binding.doneButton.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.receipt.observe(viewLifecycleOwner) {
            binding.loadingView.root.isVisible = it is Result.Loading
            binding.content.isVisible = it is Result.Success
            binding.errorView.root.isVisible = it is Result.Error

            if (it is Result.Success) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.pace_cloud_sdk_file_provider", it.data), "image/*")
                }

                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), R.string.summary_show_receipt_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
