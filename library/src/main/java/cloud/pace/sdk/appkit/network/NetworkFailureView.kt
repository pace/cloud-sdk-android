package cloud.pace.sdk.appkit.network

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import cloud.pace.sdk.R
import cloud.pace.sdk.databinding.NetworkFailureViewBinding

class NetworkFailureView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val binding = NetworkFailureViewBinding.inflate(LayoutInflater.from(context), this)
    var onClick: (() -> Unit)? = null

    init {
        findViewById<ProgressButton>(R.id.tryAgainButton).setOnClickListener {
            onClick?.invoke()
            binding.tryAgainButton.showLoading()

            Handler().postDelayed({
                binding.tryAgainButton.stopShowingLoading()
            }, 500)
        }
    }

    fun setButtonClickListener(onClick: () -> Unit) {
        this.onClick = onClick
    }
}
