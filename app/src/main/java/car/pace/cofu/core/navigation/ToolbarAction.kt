package car.pace.cofu.core.navigation

import android.content.ContextWrapper
import androidx.appcompat.widget.Toolbar
import androidx.databinding.BindingAdapter
import car.pace.cofu.R
import car.pace.cofu.ui.main.MainActivityOld

enum class ToolbarAction {
    NONE,
    BACK,
    MENU
}

@BindingAdapter("navigationAction")
fun setNavigationAction(toolbar: Toolbar, navigationAction: ToolbarAction) {
    val baseActivity = when (val ctx = toolbar.context) {
        is MainActivityOld -> ctx
        is ContextWrapper -> ctx.baseContext as? MainActivityOld
        else -> null
    } ?: return

    when (navigationAction) {
        ToolbarAction.NONE -> {
            baseActivity.menuSwipingActive = false
        }

        ToolbarAction.BACK -> {
            baseActivity.menuSwipingActive = false
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            toolbar.setNavigationOnClickListener {
                baseActivity.onBackPressed()
            }
        }

        ToolbarAction.MENU -> {
            baseActivity.menuSwipingActive = true
            toolbar.setNavigationIcon(R.drawable.ic_menu_filled)
            toolbar.setNavigationOnClickListener {
                baseActivity.toggleMenu()
            }
        }
    }
}
