package cloud.pace.sdk.appkit.network

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import cloud.pace.sdk.R

/**
 * A custom button to show loading when wanted.
 */
class ProgressButton : AppCompatButton {

    private var mText: String? = null
    private var mLoadingProgress: ProgressBar? = null
    private var mContext: Context? = null

    constructor(context: Context) : super(context) {
        this.mContext = context
        mText = text.toString()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.mContext = context
        mText = text.toString()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.mContext = context
        mText = text.toString()
    }

    /**
     * Starts the progress of showing a loading progress.
     * Note: this disables the button until stopShowingLoading() is called.
     */
    fun showLoading() {
        isEnabled = false

        if (parent is RelativeLayout) {
            mText = text.toString()
            text = ""

            mLoadingProgress = ProgressBar(mContext)
            mLoadingProgress!!.isIndeterminate = true
            mLoadingProgress!!.indeterminateDrawable.setColorFilter(ContextCompat.getColor(context, R.color.text_light), android.graphics.PorterDuff.Mode.MULTIPLY)

            val size = height - OFFSET

            val layoutParams = RelativeLayout.LayoutParams(size, size)
            layoutParams.addRule(RelativeLayout.ALIGN_TOP, this.id)
            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, this.id)
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, this.id)

            mLoadingProgress!!.layoutParams = layoutParams

            // TODO: This is not working in a LinearLayout...
            (parent as ViewGroup).addView(mLoadingProgress)
        }
    }

    /**
     * Stops the process of showing a loading progress.
     * Note: Call this to enable the button after showLoading().
     */
    fun stopShowingLoading() {
        isEnabled = true
        if (mLoadingProgress != null) {
            mLoadingProgress!!.visibility = View.INVISIBLE
            mLoadingProgress = null
        }
        text = mText
    }

    companion object {
        private const val OFFSET = 15
    }
}
