package car.pace.cofu.core.navigation.safeargs

import androidx.navigation.NavArgs

/**
 * Implement this when your ViewModel is expecting arguments to update it's state. Keep in mind that
 * you also have to implement [SafeArgsAware] in your fragment to resolve the arguments, and call
 * [onSafeArgsReceived] (after the viewModel is instantiated) in your fragment to pass the arguments.
 *
 * This should ONLY be implemented by ViewModel!
 */
interface SafeArgsReceiver<Args : NavArgs> {
    /**
     * Receives the [args] to change/update it's state.
     */
    fun onSafeArgsReceived(args: Args)
}