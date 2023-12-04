package car.pace.cofu.ui.more

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import car.pace.cofu.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class MenuItem(
    @DrawableRes val icon: Int,
    val title: MenuTitle,
    val action: MenuItemAction
)

sealed class MenuTitle {
    data class TitleString(val title: String) : MenuTitle()
    data class TitleStringRes(@StringRes val titleRes: Int) : MenuTitle()
}

sealed class MenuItemAction {
    data object Dependencies : MenuItemAction()
    data class LocalContent(val url: String) : MenuItemAction()
    data class WebContent(val url: String) : MenuItemAction()
}

@HiltViewModel
class MoreViewModel @Inject constructor() : ViewModel() {
    var entries: List<MenuItem> = emptyList()

    init {
        // TODO: Get entries from config
        entries = listOf(
            MenuItem(R.drawable.ic_developer_guide, MenuTitle.TitleStringRes(R.string.MENU_ITEMS_TERMS), getLocalContent("terms.html")),
            MenuItem(R.drawable.ic_lock, MenuTitle.TitleStringRes(R.string.MENU_ITEMS_PRIVACY), getLocalContent("privacy.html")),
            MenuItem(R.drawable.ic_domain, MenuTitle.TitleStringRes(R.string.MENU_ITEMS_IMPRINT), getLocalContent("impressum.html")),
            MenuItem(R.drawable.ic_two_pager, MenuTitle.TitleStringRes(R.string.MENU_ITEMS_LICENCES), MenuItemAction.Dependencies),
            MenuItem(R.drawable.ic_language, MenuTitle.TitleString("PACE Website"), MenuItemAction.WebContent("https://www.pace.car/de/drive/"))
        )
    }

    private fun getLocalContent(filename: String) = MenuItemAction.LocalContent("file:///android_res/raw/$filename")
}
