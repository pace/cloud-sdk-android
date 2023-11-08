package car.pace.cofu.core.navigation.safeargs

import androidx.navigation.NavArgs

/**
 * Implement this interface in Fragments which is expecting safe arguments and it is up to you how you
 * handle them. This interfaces main goal is to tell the developer that safe arguments are used in
 * this fragment. So the developer is AWARE of the safe arguments.
 *
 * The developer can decide on his own how (s)he handles the received arguments.
 *
 * Just overwrite it with the following:  override val args: Args by navArgs()
 * And if the arguments are nullable use: override val args: Args by optionalNavArgs()
 *
 * The implementation can be found here: [optionalNavArgs]
 *
 * This should only be implemented by Fragments!
 *
 * The wanted Arguments for SafeArgs are defined in the nav_graph in your resources.
 *
 * Check out further information:
 *  @see <a href="https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args">Android Developer Reference</a>
 *  @see <a href="https://medium.com/androiddevelopers/navigating-with-safeargs-bf26c17b1269">Medium Article</a>
 */
interface SafeArgsAware<Args : NavArgs> {
    val args: Args?
}
