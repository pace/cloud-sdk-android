package car.pace.cofu.ui.home

import car.pace.cofu.R
import car.pace.cofu.core.mvvm.BaseItemViewModel

class SimpleTextItemViewModel(val text: String) : BaseItemViewModel() {
    override val layoutId = R.layout.item_simple_text
    override val item: Any get() = text
}