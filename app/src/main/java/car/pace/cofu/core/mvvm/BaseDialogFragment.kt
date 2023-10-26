package car.pace.cofu.core.mvvm

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import car.pace.cofu.BR
import car.pace.cofu.R
import car.pace.cofu.core.events.ActivityEvent
import car.pace.cofu.core.events.ConsumeFragmentBackPress
import car.pace.cofu.core.events.Event
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.events.FragmentResultable
import car.pace.cofu.core.events.OpenFromFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.reflect.KClass
import timber.log.Timber

/**
 * Represents a base dialog fragment. Extending fragments should always be combined with a viewModel,
 * that's why offering the fragment layout resource id and viewModel is mandatory.
 * The viewModel updates the ui via `Databinding`.
 *
 * Dialogs can have different appearing types, choose it via setting [dialogMode].
 *
 * To use the binding of the ui after inflating in sub classes, use [getBinding].
 *
 * As the base fragment is capable of observing events fired in sub classes of [BaseViewModel]s attached to the
 * fragment, there are several stub methods which can be overridden to make use of these events. This way, the
 * viewModel does not have to directly call Android Apis but can invoke event callbacks in the activity to separate
 * all concerns.
 *
 *  @author Thomas Hofmann
 */
abstract class BaseDialogFragment<out T : ViewDataBinding, E : BaseViewModel>(
    @LayoutRes private val bindingLayoutId: Int,
    private val clazz: KClass<E>,
    private val dialogMode: DialogMode = DialogMode.DIALOG,
    private val cancellable: Boolean = true
) : BottomSheetDialogFragment(), BaseComposable {

    private var binding: ViewDataBinding? = null

    val viewModel: E
        get() = ViewModelProvider(this).get(clazz.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (dialogMode == DialogMode.DIALOG || dialogMode == DialogMode.DIALOG_FULL_SCREEN) return null

        super.onCreateView(inflater, container, savedInstanceState)

        return createView(inflater, container, savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (dialogMode == DialogMode.BOTTOM_SHEET) {
            setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
        }

        if (this is FragmentResultable) {
            setFragmentResultListener(getResultRequestKey()) { _, bundle ->
                this.onFragmentResult(bundle)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = when (dialogMode) {
        DialogMode.DIALOG, DialogMode.DIALOG_FULL_SCREEN -> setupDialog(savedInstanceState)
        DialogMode.BOTTOM_SHEET -> setupBottomSheet(savedInstanceState)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun setupBottomSheet(savedInstanceState: Bundle?) = super.onCreateDialog(
        savedInstanceState
    )
        .also { dialog ->
            // Fix to expand the bottomsheet when it is shown
            dialog.setOnShowListener { showingDialog ->
                if (showingDialog is BottomSheetDialog) {
                    val bottomSheet: FrameLayout = showingDialog
                        .findViewById(com.google.android.material.R.id.design_bottom_sheet)
                        ?: return@setOnShowListener

                    val behaviour = BottomSheetBehavior.from(bottomSheet)

                    bottomSheet.post {
                        behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                        behaviour.skipCollapsed = true
                    }
                }
            }
            dialog.setOnKeyListener(keyListener)
        }

    private val keyListener = { _: DialogInterface, keyCode: Int, event: KeyEvent ->
        var consumed = false
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            if (onConsumeBackPress()) {
                consumed = true
            }
        }
        consumed
    }

    private fun setupDialog(savedInstanceState: Bundle?): Dialog {
        return if (dialogMode == DialogMode.DIALOG_FULL_SCREEN) {
            AlertDialog.Builder(requireContext(), R.style.FullScreenDialog)
                .setView(createView(requireActivity().layoutInflater, null, savedInstanceState))
                .setOnKeyListener(keyListener)
                .create()
        } else {
            AlertDialog.Builder(requireContext())
                .setView(createView(requireActivity().layoutInflater, null, savedInstanceState))
                .setOnKeyListener(keyListener)
                .create()
                .apply {
                    window?.requestFeature(Window.FEATURE_NO_TITLE)
                }
        }
    }

    private fun createView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = cancellable
        binding = DataBindingUtil.inflate<T>(inflater, bindingLayoutId, container, false)
            .apply {
                setVariable(BR.viewModel, viewModel)
                lifecycleOwner = this@BaseDialogFragment
                executePendingBindings()
                onComposed(this, savedInstanceState)
            }

        viewModel.events.observe(
            this,
            Observer {
                handleEvent(it)
            }
        )

        return binding?.root
    }

    internal fun handleEvent(event: Event?) {
        if (event is FragmentEvent) {
            consumeFragmentEvents(event)
        } else {
            getBaseActivity()?.consumeActivityEvents(event as ActivityEvent)
        }
    }

    /**
     * Consumes an [event] emitted by a viewModel on fragment level.
     */
    private fun consumeFragmentEvents(event: FragmentEvent) {
        Timber.i("Event observed, consumed in Dialog: $event")
        when (event) {
            is OpenFromFragment -> openUp(event)
            is ConsumeFragmentBackPress -> {
                if (!onConsumeBackPress()) {
                    requireActivity().onBackPressed()
                }
            }
            else -> {
                onHandleFragmentEvent(event)
            }
        }
    }

    /**
     * Called when a back press occurred. If the back press should be consumed, return true, else return false and the
     * back press will be handled by the activity as usual.
     */
    open fun onConsumeBackPress(): Boolean {
        return false
    }

    private fun getBaseActivity(): BaseActivity<*, *>? {
        return (requireActivity() as? BaseActivity<*, *>)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getBinding(): T? {
        return binding as? T
    }

    override fun <T> onComposed(binding: T, savedInstanceState: Bundle?) {
        // to be implemented in sub classes
    }

    /**
     * Called when the fragment should handle a [FragmentEvent] coming from a [BaseViewModel].
     */
    open fun onHandleFragmentEvent(event: FragmentEvent) {
        // to be implemented in sub class
    }

    /**
     * Dismisses the dialog and saves the given bundle as a result.
     *
     * The [car.pace.cofu.base.BaseFragment] upon which this dialog is shown will then retrieve the result.
     * Please note that this will only work in these fragments and that if the dialog is started elsewhere the
     * retrieving logic has to be implemented there as well.
     *
     * Receiving fragments have to implement [FragmentResultable] for this to share a key where the request of the
     * result will be matched, that's why a [resultRequestKey] here should match the one which wants to receive it.
     */
    fun dismissDialogWithResult(resultRequestKey: String, resultBundle: Bundle) {
        setFragmentResult(resultRequestKey, resultBundle)
        dismiss()
    }

    /**
     * Lists all possible dialog modes
     */
    enum class DialogMode {

        /**
         * A bottom sheet opens from the bottom and may allow for picking.
         */
        BOTTOM_SHEET,

        /**
         * A dialog interrupts the current flow and is displayed in the center.
         */
        DIALOG,

        /**
         * Same as DIALOG but will stretch to the full screen.
         */
        DIALOG_FULL_SCREEN
    }
}
