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

    val menuHeader = ObservableField(resourcesProvider.getString(R.string.MENU_TITLE_PLACEHOLDER))
    val menuIconRes = ObservableInt(0)
    val menuEmailText = ObservableField(userDataRepository.email ?: "")

    init {
        menuAllItems.add(MenuItemViewModel(this, R.string.MENU_ITEMS_FUEL_TYPE, R.drawable.ic_fuel_menu))
        menuAllItems.add(MenuItemViewModel(this, R.string.MENU_ITEMS_PAYMENT_METHODS, R.drawable.ic_payment_menu))
        menuAllItems.add(MenuItemViewModel(this, R.string.MENU_ITEMS_PAYMENT_HISTORY, R.drawable.ic_payment_history))
        menuAllItems.add(MenuItemViewModel(this, R.string.MENU_ITEMS_IMPRINT, R.drawable.ic_imprint, true))
        menuAllItems.add(MenuItemViewModel(this, R.string.MENU_ITEMS_PRIVACY, R.drawable.ic_privacy))
        menuAllItems.add(MenuItemViewModel(this, R.string.MENU_ITEMS_LICENCES, R.drawable.ic_licenses))
    }

    fun loadUserEmail() {
        if (userDataRepository.email.isNotNullOrBlank()) {
            menuEmailText.set(userDataRepository.email ?: "")
        } else {
            IDKit.cachedToken()?.let {
                IDKit.userInfo(additionalHeaders = mapOf("Authorization" to "Bearer $it")) {
                    if (it is Success && it.result.email.isNotNullOrBlank()) {
                        userDataRepository.email = it.result.email
                        menuEmailText.set(it.result.email)
                    }
                }
            }
        }
    }

    class MenuItemClickEvent(val menuItemId: Int) : ActivityEvent()
}
