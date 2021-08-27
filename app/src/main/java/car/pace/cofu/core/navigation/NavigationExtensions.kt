package car.pace.cofu.core.navigation

import android.util.Log
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import car.pace.cofu.R
import car.pace.cofu.core.events.DismissSnackbars
import car.pace.cofu.core.events.NavigateToDirection
import car.pace.cofu.core.mvvm.BaseDialogFragment
import car.pace.cofu.core.mvvm.BaseFragment

/**
 * Navigates to the specified destination defined in [navigateTo].
 */
fun Fragment.navigate(navigateTo: NavigateToDirection, @IdRes hostResId: Int = R.id.fragment_container) {
    val navController = try {
        view?.findNavController()
    } catch (exception: Exception) {
        // fix for fragments navigating within other fragments,
        // needs setting of the id of the nav host fragment
        Log.i("NavigationExtension", "Could not find nav controller, using parent fragment manager instead")
        val host: Fragment? = parentFragmentManager.findFragmentById(hostResId)
        host?.let(NavHostFragment::findNavController)
    }

    if (navController == null) {
        Log.e("NavigationExtension", "Could not find nav controller!")
    } else {
        NavigationUtils.navigateTo(navController, navigateTo)
        (this as? BaseFragment<*, *>)?.handleEvent(DismissSnackbars())
        (this as? BaseDialogFragment<*, *>)?.handleEvent(DismissSnackbars())
    }
}