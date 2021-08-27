package car.pace.cofu.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import car.pace.cofu.R
import car.pace.cofu.core.events.NavigateToDirection
import timber.log.Timber

object NavigationUtils {

    /**
     * Lists all possible animation types for navigation destinations.
     */
    enum class AnimType {
        DEFAULT,
//        MODAL,
//        SLIDE,
//        NONE
    }

    /**
     * Navigates to the destination described in the [NavigateToDirection] event with the given [NavController].
     *
     * **Note:** Assume that popping up from this navigation to be always inclusive and pop it from
     * the back stack.
     *
     * @param navController the nav controller to use for the navigation
     * @param navigateTo the object to use for directions and actions
     */
    fun navigateTo(navController: NavController, navigateTo: NavigateToDirection) {
        val navOptions = setupOptions(
            navigateTo.clearBackStack,
            navController,
            navigateTo.navOptions,
            navigateTo.navigationPopupToId,
            navigateTo.animType
        )

        try {
            navController.navigate(
                navigateTo.navigationDirection.actionId,
                navigateTo.navigationDirection.arguments.takeIf { it.size() > 0 },
                navOptions,
                navigateTo.extras
            )
        } catch (e: Exception) {
            Timber.e(e, "Could somehow not navigate because of unknown nav destination.")
        }
    }

    private fun setupOptions(
        clearBackStack: Boolean,
        navController: NavController,
        navOptions: NavOptions?,
        popupToId: Int?,
        animType: AnimType
    ): NavOptions {
        return if (navOptions == null) {
            NavOptions.Builder()
                .apply {
                    setAnims(animType, this)

                    if (clearBackStack) {
                        setPopUpTo(navController.graph.id, true)
                    } else {
                        popupToId?.let { setPopUpTo(it, true) }
                    }
                }
                .build()
        } else {
            NavOptions.Builder()
                .apply {
                    setAnims(animType, this)

                    setLaunchSingleTop(navOptions.shouldLaunchSingleTop())

                    if (clearBackStack) {
                        setPopUpTo(navController.graph.id, true)
                    } else {
                        popupToId?.let { setPopUpTo(it, true) }
                    }
                }
                .build()
        }
    }

    /**
     * Sets the defined animations defined by [animType] into the NavOptions given by [builder]
     */
    private fun setAnims(animType: AnimType, builder: NavOptions.Builder) {
        when (animType) {
            AnimType.DEFAULT -> builder.apply {
                setEnterAnim(R.anim.nav_default_enter_anim)
                setExitAnim(R.anim.nav_default_exit_anim)
                setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                setPopExitAnim(R.anim.nav_default_pop_exit_anim)
            }
//            AnimType.MODAL -> builder.apply {
//                setEnterAnim(R.anim.alternative_enter_anim)
//                setExitAnim(R.anim.alternative_exit_anim)
//                setPopEnterAnim(R.anim.alternative_pop_enter_anim)
//                setPopExitAnim(R.anim.alternative_pop_exit_anim)
//            }
//            AnimType.SLIDE -> builder.apply {
//                setEnterAnim(R.anim.translate_in_from_right)
//                setExitAnim(R.anim.translate_out_to_left)
//                setPopEnterAnim(R.anim.translate_in_from_left)
//                setPopExitAnim(R.anim.translate_out_to_right)
//            }
//            AnimType.NONE -> builder.apply {
//                setEnterAnim(R.anim.fastfade_enter_anim)
//                setExitAnim(R.anim.fastfade_exit_anim)
//                setPopEnterAnim(R.anim.fastfade_pop_enter_anim)
//                setPopExitAnim(R.anim.fastfade_pop_exit_anim)
//            }
        }
    }
}