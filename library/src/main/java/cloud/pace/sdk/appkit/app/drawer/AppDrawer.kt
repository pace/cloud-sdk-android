package cloud.pace.sdk.appkit.app.drawer

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.transition.Slide
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import cloud.pace.sdk.R
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.Event
import cloud.pace.sdk.utils.dp
import kotlinx.android.synthetic.main.app_drawer.view.*
import org.koin.core.component.inject

class AppDrawer(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs), CloudSDKKoinComponent {

    private val viewModel: AppDrawerViewModel by inject()
    private val minBackgroundWidth = context.resources.getDimension(R.dimen.app_drawer_height).toInt()
    private var expandModeClickListener: OnClickListener? = null
    private var maxBackgroundWidth = 0
    private var initialX = 0f
    private var currentWidth = 0
    private var shouldExpand: Boolean? = null
    private var isExpanded = false
    private var cancelClick = false

    var title: String = ""
        set(value) {
            field = value
            title_view.text = value
            requestLayout()
        }

    var subtitle: String? = null
        set(value) {
            field = value
            subtitle_view.text = value
            requestLayout()
        }

    var icon: Int = R.drawable.ic_default
        set(value) {
            field = value
            icon_button.setImageResource(value)
            requestLayout()
        }

    @ColorInt
    var iconTint: Int = context.getColor(R.color.highlight)
        set(value) {
            field = value
            icon_button.backgroundTintList = ColorStateList.valueOf(value)
            requestLayout()
        }

    @ColorInt
    var backgroundTint: Int = context.getColor(R.color.background_dark)
        set(value) {
            field = value
            expanded_background.backgroundTintList = ColorStateList.valueOf(value)
            requestLayout()
        }

    private val titleObserver = Observer<String> {
        title = it
    }

    private val subtitleObserver = Observer<String?> {
        subtitle = it
    }

    private val backgroundObserver = Observer<Int> {
        backgroundTint = it
    }

    private val iconBackgroundObserver = Observer<Int> {
        iconTint = it
    }

    private val logoObserver = Observer<Bitmap> {
        setIcon(it)
    }

    private val textColorObserver = Observer<Int> {
        title_view.setTextColor(it)
        subtitle_view.setTextColor(it)
        close_button_icon.imageTintList = ColorStateList.valueOf(it)
    }

    private val closeEventObserver = Observer<Event<Unit>> {
        if (!it.hasBeenHandled) {
            (parent as? ViewGroup)?.removeView(this)
        }
    }

    init {
        inflate(context, R.layout.app_drawer, this)
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.AppDrawer,
            0, 0
        ).apply {

            try {
                title = getString(R.styleable.AppDrawer_title) ?: ""
                subtitle = getString(R.styleable.AppDrawer_subtitle)
                icon = getResourceId(R.styleable.AppDrawer_icon, R.drawable.ic_default)
                iconTint = context.getColor(getResourceId(R.styleable.AppDrawer_iconTint, R.color.highlight))
                backgroundTint = context.getColor(R.color.highlight)
            } finally {
                recycle()
            }
        }

        maxBackgroundWidth = resources.displayMetrics.widthPixels - START_MARGIN.dp
        currentWidth = maxBackgroundWidth

        icon_button.setOnClickListener {
            if (currentWidth == maxBackgroundWidth && !cancelClick) {
                expandModeClickListener?.onClick(it)
            }
        }

        expanded_background.setOnClickListener {
            if (currentWidth == maxBackgroundWidth && !cancelClick) {
                expandModeClickListener?.onClick(it)
            }
        }

        close_button.setOnClickListener {
            if (!cancelClick)
                animateView(false)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        viewModel.onCreate()

        viewModel.title.observeForever(titleObserver)
        viewModel.subtitle.observeForever(subtitleObserver)
        viewModel.background.observeForever(backgroundObserver)
        viewModel.iconBackground.observeForever(iconBackgroundObserver)
        viewModel.textColor.observeForever(textColorObserver)
        viewModel.logo.observeForever(logoObserver)
        viewModel.closeEvent.observeForever(closeEventObserver)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        viewModel.onDestroy()

        viewModel.title.removeObserver(titleObserver)
        viewModel.subtitle.removeObserver(subtitleObserver)
        viewModel.background.removeObserver(backgroundObserver)
        viewModel.iconBackground.removeObserver(iconBackgroundObserver)
        viewModel.textColor.removeObserver(textColorObserver)
        viewModel.logo.removeObserver(logoObserver)
        viewModel.closeEvent.removeObserver(closeEventObserver)
    }

    /**
     * Sets the needed app data and the OnClickListener
     */
    fun setApp(app: App, darkBackground: Boolean, listener: OnClickListener) {
        viewModel.init(app, darkBackground)
        setExpandedModeOnClickListener(listener)
    }

    /**
     * Sets the AppDrawer button icon
     */
    private fun setIcon(bitmap: Bitmap) {
        icon_button.setImageBitmap(bitmap)
    }

    /**
     * Specify what should happen when the user clicks on the view in expanded mode
     */
    fun setExpandedModeOnClickListener(listener: OnClickListener) {
        expandModeClickListener = listener
    }

    fun show() {
        val transition = Slide(Gravity.END)
        transition.addTarget(this)
        val parent = parent as? ViewGroup
        parent?.let {
            TransitionManager.beginDelayedTransition(it, transition)
            this.visibility = View.VISIBLE
        }
    }

    /**
     * Expands the AppDrawer
     */
    fun expand() {
        animateView(true)
    }

    /**
     * Collapses the AppDrawer
     */
    fun collapse() {
        animateView(false)
    }

    private fun animateIconBackground(expand: Boolean) {
        val drawable = icon_button.background as GradientDrawable
        val radius = context.resources.getDimension(R.dimen.app_drawer_icon_radius)
        val animator = if (expand) ValueAnimator.ofFloat(0f, radius) else ValueAnimator.ofFloat(radius, 0f)
        animator.setDuration(500)
            .addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                drawable.cornerRadii = floatArrayOf(radius, radius, value, value, value, value, radius, radius)
            }
        animator.start()
    }

    /**
     * Swipe logic
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = it.rawX
                }

                MotionEvent.ACTION_MOVE -> {
                    when {
                        it.rawX > initialX -> {
                            shouldExpand = false
                            val diffInPx = it.rawX - initialX
                            if (currentWidth == maxBackgroundWidth && diffInPx < TOUCH_TOLERANCE) return true

                            val newWidth = currentWidth - diffInPx

                            if (newWidth > minBackgroundWidth) {
                                expanded_background.layoutParams.width = newWidth.toInt()
                                expanded_background.layoutParams = expanded_background.layoutParams
                                currentWidth = newWidth.toInt()
                                initialX = it.rawX
                            } else true
                        }

                        it.rawX < initialX -> {
                            shouldExpand = true
                            val diffInPx = initialX - it.rawX
                            val newWidth = currentWidth + diffInPx

                            if (newWidth < maxBackgroundWidth) {
                                expanded_background.layoutParams.width = newWidth.toInt()
                                expanded_background.layoutParams = expanded_background.layoutParams
                                currentWidth = newWidth.toInt()
                                initialX = it.rawX
                            } else true
                        }
                        else -> true
                    }
                }

                MotionEvent.ACTION_UP -> {
                    when {
                        currentWidth <= minBackgroundWidth + TOUCH_TOLERANCE -> animateView(true)
                        currentWidth != maxBackgroundWidth -> shouldExpand?.let {
                            animateView(it)
                        }
                        else -> true
                    }
                }
                else -> true
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun animateView(expand: Boolean) {
        val endWidth = if (expand) maxBackgroundWidth else minBackgroundWidth
        val anim = ValueAnimator.ofInt(currentWidth, if (expand) maxBackgroundWidth else minBackgroundWidth)

        anim.duration = 200
        anim.addUpdateListener {
            val animatedValue: Int = it.animatedValue as Int
            val layoutParams = this.expanded_background.layoutParams
            layoutParams.width = animatedValue
            this.expanded_background.layoutParams = layoutParams
        }
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {
                cancelClick = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                cancelClick = false
                handleCloseButton(expand)
            }
        })
        anim.start()

        currentWidth = endWidth
        this.shouldExpand = expand

        // Only animate icon background if there is a transition (collapsed -> expanded, expanded -> collapsed)
        if (isExpanded != expand) {
            animateIconBackground(expand)
        }
        isExpanded = expand
    }

    private fun handleCloseButton(enable: Boolean) {
        close_button_icon.visibility = if (enable) View.VISIBLE else View.INVISIBLE
        close_button.visibility = if (enable) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TOUCH_TOLERANCE = 50
        private const val START_MARGIN = 20f
    }
}
