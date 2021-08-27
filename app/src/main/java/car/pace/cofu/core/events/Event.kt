package car.pace.cofu.core.events

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import car.pace.cofu.core.navigation.NavigationUtils
import car.pace.cofu.util.snacker.Snacker
import kotlin.reflect.KClass

/**
 * A collection of commonly used events.
 */
sealed class Event

/**
 * A event which should be handled by a fragment.
 */
open class FragmentEvent : Event()

/**
 * A event which should be handled by an activity.
 */
open class ActivityEvent : Event()

/**
 * Object for navigation. Navigation arguments are passed as SafeArgs within the [navigationDirection]
 *
 * @param navigationDirection the target to navigate to. May reference a destination or action
 * @param clearBackStack clears the back stack. Note that you can not use [navigationPopupToId] when this is set to true
 * @param navOptions optional navigator options for setting the default animations and behaviour
 * @param extras options for the transaction, like shared views for transitions
 * @param animType a type of animation, defaults to system animations
 * @param navigationPopupToId id for target to popup to. This will only work, if [clearBackStack] is set to false
 */
data class NavigateToDirection(
    val navigationDirection: NavDirections,
    val clearBackStack: Boolean = false,
    val navOptions: NavOptions? = null,
    val extras: Navigator.Extras? = null,
    val animType: NavigationUtils.AnimType = NavigationUtils.AnimType.DEFAULT,
    @IdRes val navigationPopupToId: Int? = null
) : FragmentEvent()

/**
 * An event for opening a screen or another activity.
 *
 * @param url a url to open up in a new browser window
 * @param clazz an activity class to open up
 * @param args optional arguments for the new screen
 */
data class OpenFromFragment(
    val url: String? = null,
    val clazz: KClass<*>? = null,
    val args: Bundle? = null
) : FragmentEvent()

/**
 * An event to indicate that a back press has been registered for a fragment.
 *
 * The optional [consumed] flag can indicate whether this event should be evaluated or not.
 */
data class ConsumeFragmentBackPress(val consumed: Boolean = true) : FragmentEvent()

/**
 * An event to indicate that a back press has been registered for an activity.
 *
 * The optional [consumed] flag can indicate whether this event should be evaluated or not.
 */
data class ConsumeActivityBackPress(val consumed: Boolean = true) : ActivityEvent()

/**
 * An event to indicate that a close request of the current screen has been registered.
 *
 * The optional [consumed] flag can indicate whether this event should be evaluated or not.
 */
data class Close(val consumed: Boolean = true) : ActivityEvent()

/**
 * An event for showing a toast.
 *
 * @param message the message to show
 */
data class ShowToast(val message: String) : ActivityEvent()

/**
 * An event for showing a message, generally a snack bar of some sorts.
 *
 * @param message the message to show
 * @param type the message type. Different types have different styles. Defaults to [Snacker.SnackType.DEFAULT]
 * @param actionText optional text for an action which may be shown next to the message
 * @param actionListener optional listener for the action to perform on a click of the [actionText].
 * May only be useful if such a text is given.
 */
data class ShowSnack(
    val message: String? = null,
    val messageRes: Int? = null,
    val type: Snacker.SnackType = Snacker.SnackType.DEFAULT,
    val actionText: String? = null,
    val actionListener: (() -> Unit)? = null
) : ActivityEvent()

/**
 * Dismisses all currently displayed snackbars
 */
class DismissSnackbars: ActivityEvent()

/**
 * Closes the navigation drawer
 */
class CloseDrawer: ActivityEvent()

/**
 * An event for showing a un-styled alert dialog with the following options.
 *
 * @param title the alert dialog title
 * @param message the alert dialog message
 * @param positiveButtonText an optional positive button text, use at `null` for a generic one
 * @param negativeButtonText an optional negative button text, use `null` for a generic one
 * @param view the content view of alert dialog
 * @param onPositiveClicked function to invoke when positive button of alert dialog is pressed
 */
data class ShowAlertDialog(
    val title: String,
    val message: String,
    val positiveButtonText: String? = null,
    val negativeButtonText: String? = null,
    val view: View? = null,
    val onPositiveClicked: () -> Unit
) : ActivityEvent()


