package cloud.pace.sdk.appkit.network

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import cloud.pace.sdk.R
import kotlinx.android.synthetic.main.network_failure_view.view.*

class NetworkFailureView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    var onClick: (() -> Unit)? = null

    init {
        View.inflate(context, R.layout.network_failure_view, this)

        findViewById<ProgressButton>(R.id.tryAgainButton).setOnClickListener {
            onClick?.invoke()
            tryAgainButton.showLoading()

            Handler().postDelayed({
                tryAgainButton?.stopShowingLoading()
            }, 500)
        }
    }

    fun setButtonClickListener(onClick: () -> Unit) {
        this.onClick = onClick
    }
}
