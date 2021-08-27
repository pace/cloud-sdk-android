package car.pace.cofu.core.mvvm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import car.pace.cofu.BR
import car.pace.cofu.core.events.*
import timber.log.Timber
import kotlin.reflect.KClass

/**
 * Represents a base fragment. Extending fragments should always be combined with a viewModel,
 * that's why offering the fragment layout resource id and viewModel is mandatory.
 * The viewModel updates the ui via `Databinding`.
 *
 * To use the binding of the ui after inflating in sub classes, use [getBinding].
 *
 * As the base fragment is capable of observing events fired in sub classes of [BaseViewModel]s attached to the
 * fragment, there are several stub methods which can be overridden to make use of these events. This way, the
 * viewModel does not have to directly call Android Apis but can invoke event callbacks in the activity to separate
 * all concerns.
 *
 * @param bindingLayoutId the layout resource id of the binding which binds the layout for data changes to this fragment
 * @param clazz the viewModel intended to update the layout of this fragment with data mappings through databinding
 * @param useActivityPool Determines if this viewModel should be stored in the activity scope or not. If set to false will store the
 * viewModel in the scope of the current fragment or activity (depends on what it is attached to) and release it,
 * if the fragment or activity is no longer needed. This is the default setting.
 * If set to true, it uses the scope of a activity, meaning that it will only become useless, if the activity
 * containing the fragment or the activity attached) with this viewModel is no longer needed.
 * This may be useful for sharing the viewModel data across screens.
 * If you want to tie the view model to a parent fragment instead, use the [useParentFragment] flag instead.
 *
 * Please note that you should only set `useActivityPool` OR `useParentFragment`. Setting both to true will result in
 * unexpected behaviour. The default is setting both to false (and therefore not using it).
 *
 * @author Thomas Hofmann
 */
abstract class BaseFragment<out T : ViewDataBinding, E : BaseViewModel>(
    @LayoutRes private val bindingLayoutId: Int,
    private val clazz: KClass<E>,
    private val useActivityPool: Boolean = false,
    private val useParentFragment: Boolean = false
) : Fragment(), BaseComposable {

    private var binding: ViewDataBinding? = null

    val viewModel: E
        get() = when {
            useActivityPool -> ViewModelProvider(requireActivity()).get(clazz.java)
            useParentFragment -> ViewModelProvider(requireParentFragment().requireParentFragment()).get(
                clazz.java
            )
            else -> ViewModelProvider(this).get(clazz.java)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (this is FragmentResultable) {
            setFragmentResultListener(getResultRequestKey()) { _, bundle ->
                this.onFragmentResult(bundle)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate(layoutInflater, bindingLayoutId, null, true)
        binding?.apply {
            setVariable(BR.viewModel, viewModel)
            lifecycleOwner = viewLifecycleOwner
            executePendingBindings()
            onComposed(this, savedInstanceState)
        }

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!onConsumeBackPress()) {
                        isEnabled = false // don't allow for recursions of back press
                        requireActivity().onBackPressed()
                        isEnabled = true
                    }
                }
            })

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.events.observe(
            viewLifecycleOwner,
            Observer {
                handleEvent(it)
            }
        )
    }

    internal fun handleEvent(it: Event?) {
        if (it is FragmentEvent) {
            consumeFragmentEvents(it)
        } else {
            getBaseActivity()?.consumeActivityEvents(it as ActivityEvent)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    /**
     * Consumes an [event] emitted by a viewModel on fragment level.
     */
    private fun consumeFragmentEvents(event: FragmentEvent) {
        Timber.i("Event observed, consumed in Fragment: $event")
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
}
