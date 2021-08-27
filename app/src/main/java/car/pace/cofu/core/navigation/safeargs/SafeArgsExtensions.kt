package car.pace.cofu.core.navigation.safeargs

import android.os.Bundle
import androidx.annotation.MainThread
import androidx.collection.ArrayMap
import androidx.fragment.app.Fragment
import androidx.navigation.NavArgs
import androidx.navigation.navArgs
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * This file is a modification to google `by navArgs()` which does NOT allow a fragment to receive
 * optional arguments. With this modification it is better suited for the JamitLabs Android Architecture
 *
 * Example:
 * - Navigate to a screen from BottomNavigation -> No Arguments
 * - Navigate to the same screen with a deeplink which contains a token -> Arguments
 **/


/**
 * Returns a [Lazy] delegate to access the Fragment's arguments as an optional [Args] instance.
 *
 * It is strongly recommended that this method only be used when the Fragment is created
 * by [androidx.navigation.NavController.navigate] with the corresponding
 * [androidx.navigation.NavDirections] object, which ensures that the required
 * arguments are present.
 *
 * ```
 * class MyFragment : Fragment() {
 *     val args: MyFragmentArgs by safeNavArgs()
 * }
 * ```
 *
 * This property can be accessed only after the Fragment's constructor.
 */
@MainThread
inline fun <reified Args : NavArgs> Fragment.optionalNavArgs(): Lazy<Args?> =
    OptionalNavArgsLazy(Args::class) { arguments }



internal val methodSignature = arrayOf(Bundle::class.java)
internal val methodMap = ArrayMap<KClass<out NavArgs>, Method>()

/**
 * An implementation of [Lazy] used by [android.app.Activity.navArgs] and
 * [androidx.fragment.app.Fragment.navArgs].
 *
 * [argumentProducer] is a lambda that will be called during initialization to provide
 * arguments to construct an [Args] instance via reflection.
 */
class OptionalNavArgsLazy<Args : NavArgs>(
    private val navArgsClass: KClass<Args>,
    private val argumentProducer: () -> Bundle?
) : Lazy<Args?> {
    private var cached: Args? = null

    override val value: Args?
        get() {
            var args: Args? = cached
            if (args == null) {
                val arguments = argumentProducer()
                val method: Method = methodMap[navArgsClass]
                    ?: navArgsClass.java.getMethod("fromBundle", *methodSignature)
                        .also { method ->
                            // Save a reference to the method
                            methodMap[navArgsClass] = method
                        }

                args = try {
                    @Suppress("UNCHECKED_CAST")
                    method.invoke(null, arguments) as Args
                } catch (e: InvocationTargetException) {
                    null
                }

                cached = args
            }
            return args
        }

    override fun isInitialized() = cached != null
}