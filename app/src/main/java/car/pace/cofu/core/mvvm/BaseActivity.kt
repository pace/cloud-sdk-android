package car.pace.cofu.core.mvvm

import android.os.Bundle
import android.util.Log
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import car.pace.cofu.BR
import car.pace.cofu.core.events.ActivityEvent
import car.pace.cofu.core.events.Close
import car.pace.cofu.core.events.ConsumeActivityBackPress
import car.pace.cofu.core.events.DismissSnackbars
import car.pace.cofu.core.events.ShowAlertDialog
import car.pace.cofu.core.events.ShowSnack
import car.pace.cofu.core.events.ShowToast
import kotlin.reflect.KClass

/**
 * Represents a base activity. Extending activities should always be combined with a viewModel,
 * that's why offering the activity layout resource id and viewModel is mandatory.
 *
 * The viewModel updates the ui via `Databinding`.
 *
 * To use the binding of the ui after inflating in sub classes, use [getBinding].
 *
 * As the base activity is capable of observing events fired in sub classes of [BaseViewModel]s attached to the
 * activity, there are several stub methods which can be overridden to make use of these events. This way, the
 * viewModel does not have to directly call Android Apis but can invoke event callbacks in the activity to separate
 * all concerns.
 *
 * @param bindingLayoutId the layout resource id of the binding which binds the layout for data changes to this activity
 * @param clazz the viewModel intended to update the layout of this activity with data mappings through databinding
 *
 * @author Thomas Hofmann
 */
abstract class BaseActivity<out T : ViewDataBinding, E : BaseViewModel>(
    @LayoutRes private val bindingLayoutId: Int,
    private val clazz: KClass<E>
) : AppCompatActivity(), BaseComposable {

    private var binding: ViewDataBinding? = null

    val viewModel: E
        get() = ViewModelProvider(this).get(clazz.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.inflate(layoutInflater, bindingLayoutId, null, true)
        binding?.apply {
            setVariable(BR.viewModel, viewModel)
            lifecycleOwner = this@BaseActivity
            executePendingBindings()
            onComposed(this, savedInstanceState)
            setContentView(this.root)
        }

        viewModel.events.observe(
            this,
            Observer {
                if (it is ActivityEvent) consumeActivityEvents(it)
            }
        )

        intent?.extras?.let {
            Log.i("BaseActivity", "Received arguments, do you want to handle them?")
        }
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }

    /**
     * Consumes an [event] emitted by a viewModel on activity level.
     */
    fun consumeActivityEvents(event: ActivityEvent) {
        Log.i("BaseActivity", "Event observed, consumed in Activity: $event")
        when (event) {
            is ShowSnack -> onShowMessage(event)
            is DismissSnackbars -> dismissSnackbars()
            is ShowToast -> onShowToast(event)
            is ShowAlertDialog -> onShowAlertDialog(event)
            is Close -> onFinish()
            is ConsumeActivityBackPress -> {
                if (!onConsumeBackPress()) {
                    super.onBackPressed()
                }
            }
            else -> {
                onHandleActivityEvent(event)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getBinding(): T? {
        return binding as? T
    }

    /**
     * Called when a back press occurred. If the back press should be consumed, return true, else return false and the
     * back press will be handled by the activity as usual.
     */
    open fun onConsumeBackPress(): Boolean {
        return false
    }

    override fun <T> onComposed(binding: T, savedInstanceState: Bundle?) {
        // to be implemented in sub classes
    }

    /**
     * Called when the activity should handle a [ActivityEvent] coming from a [BaseViewModel].
     */
    open fun onHandleActivityEvent(event: ActivityEvent) {
        // to be implemented in sub class
    }

    /**
     * Called when a message with the contents [showSnack] should be shown.
     */
    open fun onShowMessage(showSnack: ShowSnack) {
        // to be implemented in sub class
    }

    /**
     * Called when a toast with the contents [showToast] should be shown.
     */
    open fun onShowToast(showToast: ShowToast) {
        // to be implemented in sub class
    }

    /**
     * Called when a fragment or viewmodel requests to close all snackbars
     */
    open fun dismissSnackbars() {
        // to be implemented in subclasses
    }

    /**
     * Called when a unstyled alert dialog with the contents [showAlertDialog] should be shown.
     */
    open fun onShowAlertDialog(showAlertDialog: ShowAlertDialog) {
        // to be implemented in sub class
    }

    /**
     * Called when the activity should be finished.
     */
    open fun onFinish() {
        // to be implemented in sub class
    }
}
