package car.pace.cofu.core.mvvm

import androidx.databinding.BaseObservable
import car.pace.cofu.BR

/**
 * Represents the base class for an item viewModel. This offers access to methods called when
 */
abstract class BaseItemViewModel : BaseObservable() {

    /**
     * id used to inflate this item's layout
     */
    abstract val layoutId: Int

    /**
     * Object for checking whether contents are the same. The returned object should override
     * `equals`
     */
    open val item: Any get() = layoutId

    /**
     * Unique id used for checking whether items are the same.
     */
    open val id: Int get() = item.hashCode()

    /**
     * This is the databinding variable, used for binding the viewModel to the view.
     */
    open val bindVar = BR.viewModel

    /**
     * Called when the item view model has been attached and is ready to be displayed.
     */
    open fun onAttached() {
        // to be implemented in subclasses
    }

    /**
     * Called when the item view model is no longer needed. Use to clear up resources.
     */
    fun onCleared() {
        // to be implemented in subclasses
    }
}
