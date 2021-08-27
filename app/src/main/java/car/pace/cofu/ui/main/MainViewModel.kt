package car.pace.cofu.ui.main

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import car.pace.cofu.R
import car.pace.cofu.core.events.ActivityEvent
import car.pace.cofu.core.mvvm.BaseItemViewModel
import car.pace.cofu.core.mvvm.BaseViewModel
import car.pace.cofu.core.resources.ResourcesProvider
import car.pace.cofu.repository.UserDataRepository
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.isNotNullOrBlank
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    resourcesProvider: ResourcesProvider,
    private val userDataRepository: UserDataRepository
) :
    BaseViewModel() {

    val onboardingDone get() = userDataRepository.onboardingDone && IDKit.isAuthorizationValid()

    val menuAllItems = ObservableArrayList<BaseItemViewModel>()

    val menuHeader = ObservableField(resourcesProvider.getString(R.string.app_name))
    val menuIconRes = ObservableInt(0)

    init {
        menuAllItems.add(MenuItemViewModel(this, R.string.menu_item_fuel_type, R.drawable.ic_fuel_menu))
        menuAllItems.add(MenuItemViewModel(this, R.string.menu_item_payment, R.drawable.ic_payment_menu))
        menuAllItems.add(MenuItemViewModel(this, R.string.menu_item_history, R.drawable.ic_payment_history))
        menuAllItems.add(MenuItemViewModel(this, R.string.menu_item_impressum, R.drawable.ic_info, true))
        menuAllItems.add(MenuItemViewModel(this, R.string.menu_item_privacy, R.drawable.ic_privacy))
        menuAllItems.add(MenuItemViewModel(this, R.string.menu_item_licenses, R.drawable.ic_licence))
        menuAllItems.add(MenuItemViewModel(this, R.string.menu_item_logout, R.drawable.ic_logout, true))

        loadUserName()
    }

    private fun loadUserName() {
        val accessToken = IDKit.cachedToken() ?: return
        // no error handling, just show the app name in header as fallback when the call fails
        IDKit.userInfo(accessToken) {
            if (it is Success && it.result.email.isNotNullOrBlank()) {
                menuHeader.set(it.result.email)
                menuIconRes.set(R.drawable.ic_account)
            }
        }
    }

    class MenuItemClickEvent(val menuItemId: Int) : ActivityEvent()
}