package car.pace.cofu.core.mvvm

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import car.pace.cofu.core.events.OpenFromFragment

/**
 * Defines all needed methods for creating a base for view binding and communication between fragment and viewModels
 * or activities and viewModels.
 **/
interface BaseComposable {

    /**
     * Retrieves the view binding. May only be useful and return results after view creation.
     */
    fun <T> getBinding(): T?

    /**
     * Called when the composition of binding has been done. Useful for additional view state manipulations.
     */
    fun <T> onComposed(binding: T, savedInstanceState: Bundle?)
}

/**
 * Opens a window with the specified info of [open] or tries to open a activity intent.
 */
fun Fragment.openUp(open: OpenFromFragment) {
    if (open.clazz != null) {

        val intent = Intent(activity, open.clazz.java)

        open.args?.let {
            intent.putExtras(it)
        }

        startActivity(intent)
    } else {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(open.url)))
    }
}

/**
 * Retrieves the base Activity, if it is of kind [BaseActivity], else returns null.
 */
fun Fragment.getBaseActivity(): BaseActivity<*, *>? {
    return (requireActivity() as? BaseActivity<*, *>)
}
