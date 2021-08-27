package car.pace.cofu.core.events

import android.os.Bundle

/**
 * Indicates that this class can process a result to fragments of type BaseFragment.
 **/
interface FragmentResultable {

    /**
     * Defines a request key for the wanted result. This key is needed to reference the correct fragment which returns
     * a result.
     */
    fun getResultRequestKey(): String

    /**
     * Called if there has been any result with the given [resultBundle] on a called fragment.
     */
    fun onFragmentResult(resultBundle: Bundle)

}