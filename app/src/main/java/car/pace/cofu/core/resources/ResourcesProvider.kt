package car.pace.cofu.core.resources

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

/**
 * A provider for resources access.
 */
interface ResourcesProvider {

    /**
     * Retrieves a quantity string.
     */
    fun getQuantityString(@PluralsRes resId: Int, quantity: Int, vararg args: Any?): String

    /**
     * Retrieves a string.
     */
    fun getString(@StringRes resId: Int): String

    /**
     * Retrieves a string with arguments.
     */
    fun getString(@StringRes resId: Int, vararg args: Any?): String

    /**
     * Retrieves a color.
     */
    fun getColor(resId: Int): Int

    /**
     * Retrieves a drawable, if possible.
     */
    fun getDrawable(resId: Int): Drawable?

    /**
     * Retrieves the dimension in pixel size.
     */
    fun getDimensionPixelSize(resId: Int): Int

    /**
     * Retrieves an integer.
     */
    fun getInteger(resId: Int): Int

    /**
     * Retrieves the application context.
     */
    fun getApplicationContext(): Context
}
