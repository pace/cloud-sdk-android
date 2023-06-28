package cloud.pace.sdk.appkit.app.drawer.ui

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import cloud.pace.sdk.appkit.AppKit.defaultAppCallback

class AppDrawerHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AbstractComposeView(context, attrs, defStyle) {

    var callback by mutableStateOf(defaultAppCallback)

    @Composable
    override fun Content() {
        AppDrawerHost(
            modifier = Modifier,
            callback = callback
        )
    }
}
