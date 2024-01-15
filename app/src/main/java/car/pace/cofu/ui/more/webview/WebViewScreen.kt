package car.pace.cofu.ui.more.webview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import car.pace.cofu.ui.component.TextTopBar
import car.pace.cofu.ui.component.WebView
import car.pace.cofu.ui.component.rememberWebViewState
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.Constants

@Composable
fun WebViewScreen(
    url: String,
    onNavigateUp: () -> Unit
) {
    Column {
        TextTopBar(
            onNavigateUp = onNavigateUp
        )

        WebView(
            state = rememberWebViewState(url),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
fun WebViewScreenPreview() {
    AppTheme {
        WebViewScreen(
            url = Constants.IMPRINT_URI,
            onNavigateUp = {}
        )
    }
}
