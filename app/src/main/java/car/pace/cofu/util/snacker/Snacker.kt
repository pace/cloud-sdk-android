package car.pace.cofu.util.snacker

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import car.pace.cofu.R
import car.pace.cofu.core.events.ShowSnack
import car.pace.cofu.util.snacker.Snacker.Companion.HIDE_TIME_MS
import car.pace.cofu.util.snacker.Snacker.SnackAnim
import car.pace.cofu.util.snacker.Snacker.SnackType
import kotlin.math.max

/**
 * Represents a snacker which animates in and automatically out after a short delay to displays short bits of
 * information. This is similar to a material snackBar but differs in styling and animation. An optional action can
 * be performed while the snacker is active, if set.
 *
 * Simply include this Snacker inside your layout and show it via [reveal]. Make sure to put the snacker
 * relatively at top of the main layouts to make it appear on top of them.
 *
 * To set the optional action, both an actionText and actionListener have to be sent through the event triggered by
 * [reveal].
 *
 * The snacker will hide itself after [HIDE_TIME_MS] ms.
 *
 * The currently used animation for revealing and hiding is built in and can only be changed inside this class by
 * hard setting the value of the private field [animType]. Use one of [SnackAnim].
 *
 * There are several types of snackers defined in [SnackType] to allow for different styles and longer hiding delays.
 */
class Snacker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    /**
     * Lists all possible built in animations for the snacker.
     */
    enum class SnackAnim {
        /**
         * A center reveal animation.
         */
        REVEAL,

        /**
         * A sliding from bottom to original position animation.
         */
        SLIDE
    }

    /**
     * Lists all possible styling types for a snacker. Can also have an impact on the delay of the hiding of the
     * snacker.
     */
    enum class SnackType(
        @ColorRes val textColorRes: Int,
        @ColorRes val actionColorRes: Int,
        @DrawableRes val backgroundRes: Int
    ) {

        /**
         * Contains the default styling and hiding duration of the snacker.
         *
         * Useful for quick and short information display after completing an action or process.
         */
        DEFAULT(
            R.color.snackDefaultTextColor,
            R.color.snackActionTextColor,
            R.drawable.bg_snacker_default
        ),

        /**
         * Contains a styling which gives a clear warning to the user.
         *
         * Useful for displaying important information which has a certain impact but does not interfere with the user.
         */
        WARNING(
            R.color.snackWarningTextColor,
            R.color.snackActionTextColor,
            R.drawable.bg_snacker_warning
        ),

        /**
         * Contains styling which gives a clear understanding that an error has occurred. Doubles the displaying time
         * to make sure the user can react to this error.
         *
         * Useful for displaying processing errors.
         */
        ERROR(
            R.color.snackErrorTextColor,
            R.color.snackActionTextColor,
            R.drawable.bg_snacker_error
        )
    }

    private val snackHandler = Handler()

    private var isHidingStopped = false
    private var snackText: TextView
    private var snackActionText: TextView
    private var container: View

    private val animType = SnackAnim.REVEAL

    init {
        inflate(context, R.layout.layout_snacker, this)

        container = findViewById(R.id.snacker)
        snackText = findViewById(R.id.snackText)
        snackActionText = findViewById(R.id.snackerActionText)
        snackText.text = ""
        snackActionText.text = ""
        snackActionText.visibility = View.GONE
        visibility = View.INVISIBLE
    }

    /**
     * Reveals the snacker. Will auto dismiss itself. A snacker can be revealed several times.
     */
    fun reveal(showSnack: ShowSnack) {
        snackHandler.removeCallbacksAndMessages(null)
        isHidingStopped = true

        container.setOnClickListener {
            snackHandler.removeCallbacksAndMessages(null)
            hide()
        }

        container.setBackgroundResource(showSnack.type.backgroundRes)
        snackText.setTextColor(ContextCompat.getColor(context, showSnack.type.textColorRes))
        snackText.text = showSnack.messageRes?.let { context.getString(it) } ?: showSnack.message
        snackActionText.visibility = View.GONE
        snackActionText.setTextColor(ContextCompat.getColor(context, showSnack.type.actionColorRes))

        showSnack.actionText?.let {
            if (showSnack.actionListener != null && it.isNotEmpty()) {
                snackActionText.apply {
                    val content = SpannableString(it)
                    content.setSpan(UnderlineSpan(), 0, content.length, 0)
                    text = content
                    visibility = View.VISIBLE
                    setOnClickListener {
                        showSnack.actionListener.invoke()
                        hide()
                    }
                }
            }
        }

        alpha = 0.0f

        post {
            alpha = 1.0f
            visibility = View.VISIBLE
            if (animType == SnackAnim.REVEAL) {
                createCenteredReveal(container)
            } else {
                slidedShow()
            }
        }

        val timeMs = if (showSnack.type == SnackType.ERROR) HIDE_TIME_MS * 2 else HIDE_TIME_MS

        snackHandler.postDelayed({
            hide()
        }, timeMs)
    }

    private fun hide() {
        isHidingStopped = false

        if (animType == SnackAnim.REVEAL) {
            createCenteredHide(container)
        } else {
            slidedHide()
        }
    }

    // Reveal animations

    // caution: these are one shot animations, they can not be reused / stopped / paused!
    private fun createCenteredReveal(view: View) {
        val bounds = Rect()
        view.getDrawingRect(bounds)
        val finalRadius = max(bounds.width(), bounds.height())
        ViewAnimationUtils.createCircularReveal(
            view,
            bounds.centerX(),
            bounds.centerY(),
            0f,
            finalRadius.toFloat()
        )
            .start()
    }

    private fun createCenteredHide(view: View) {
        val bounds = Rect()
        view.getDrawingRect(bounds)
        val initialRadius = view.width / 2

        if (!view.isAttachedToWindow) {
            return
        }

        ViewAnimationUtils.createCircularReveal(
            view,
            bounds.centerX(),
            bounds.centerY(),
            initialRadius.toFloat(),
            0f
        ).apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    if (!isHidingStopped) {
                        visibility = View.INVISIBLE
                    }
                }
            })
            start()
        }
    }

    // Slide animations
    private fun slidedShow() {
        translationY = (height * 2).toFloat()
        alpha = 0.0f
        animate().translationY(0.0f)
            .alpha(1.0f)
            .setInterpolator(DecelerateInterpolator())
            .setDuration(SLIDING_TIME_MS)
            .start()
    }

    private fun slidedHide() {
        val animateTo = (height * 2).toFloat()
        animate().translationY(animateTo)
            .alpha(0.0f)
            .setInterpolator(AccelerateInterpolator())
            .setDuration(SLIDING_TIME_MS)
            .start()
    }

    companion object {
        const val HIDE_TIME_MS = 2_500.toLong()
        const val SLIDING_TIME_MS = 300L
    }
}
