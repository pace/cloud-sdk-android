package car.pace.cofu.ui.main

import car.pace.cofu.R
import car.pace.cofu.core.mvvm.BaseItemViewModel
import car.pace.cofu.core.mvvm.BaseViewModel

class MenuItemViewModel(
    val parent: BaseViewModel,
    val text: Int,
    val iconRes: Int,
    val addDividerTop: Boolean = false
) : BaseItemViewModel() {
    override val layoutId = R.layout.item_menu

    override val item: Any get() = text
    override val id: Int get() = text

    fun onClick() {
        parent.handleEvent(MainViewModel.MenuItemClickEvent(text))
    }
}
