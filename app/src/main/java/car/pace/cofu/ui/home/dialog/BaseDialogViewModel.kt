package car.pace.cofu.ui.home.dialog

import androidx.databinding.ObservableInt
import car.pace.cofu.core.mvvm.BaseViewModel

/**
 * The base dialog view model offers access to predefined values such as title, desccription and
 * button text and click event.
 */
abstract class BaseDialogViewModel : BaseViewModel() {

    /**
     * The dialog title.
     */
    abstract val title: ObservableInt

    /**
     * The dialog description.
     */
    abstract val description: ObservableInt

    /**
     * The button text.
     */
    abstract val buttonText: ObservableInt

    /**
     * The callback for the button click.
     */
    abstract fun onButtonClick()
}