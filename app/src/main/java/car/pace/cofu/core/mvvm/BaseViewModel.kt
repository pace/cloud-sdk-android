package car.pace.cofu.core.mvvm

import androidx.lifecycle.ViewModel
import car.pace.cofu.core.events.Event
import car.pace.cofu.core.events.QueueLiveEvent

/**
 * The *base view model*. Subclasses of this ViewModel should be used to separate sub classes of [BaseActivity]s or
 * [BaseFragment]s view representation, inflated via Databinding and the business logic of the
 * app. ViewModels do not directly know the activities or fragment layouts they are responsible for updating their
 * state.
 *
 * This is, by convention, not the usual viewModel implementation proposed by Google.
 * The ViewModels here will mainly offer mappings of data to `MutableLiveData` or such which will update views via
 * Databinding. Any access to user interactions, databases and/or repository is coordinated through the ViewModel. This
 * makes it easier to test pure business logic, as there will be only such in the ViewModels which themselves do not
 * depend on or use Android Apis directly when possible, making the unit tests much easier and lightweight.
 *
 * Even if it is possible to create complex binding adapter expressions and xml to get rid of ui logic in the viewModel,
 * we encourage using the ui mapping as in its current state is easier to maintain.
 *
 * Some methods of the ViewModel will trigger or generate events which will be evaluated by the Fragment or Activity
 * attached to it. This builds the bridge for callbacks from the ViewModel to the View for Android Api events the
 * ViewModel should not handle itself, like Permission Handling and so on. In this way, the ViewModel does not know
 * the View directly and can be exchanged at any time.
 *
 * The Activity or Fragment themselves apply the ViewModels and thus can call methods of the ViewModel **directly**.
 */
abstract class BaseViewModel : ViewModel() {

    /**
     * A list of live events which can be observed by fragments and activities.
     *
     * Use this only to observe the events.
     *
     * If you want to que an event inside a viewModel, please use [handleEvent].
     */
    val events = QueueLiveEvent<Event>()

    /**
     * This takes the [event] and let's the currently active lifecycle observers
     * handle ot. Active lifecycle observers may include an Activity and/or Fragments.
     *
     * It is the responsibility of the event to define what should handle it by passing a
     * [car.pace.cofu.base.events.FragmentEvent] or a [car.pace.cofu.base.events.ActivityEvent].
     */
    fun handleEvent(event: Event) {
        events.queueValue(event)
    }
}
