package car.pace.cofu.ui.more

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.ui.component.WebView
import car.pace.cofu.ui.component.rememberWebViewState
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.Constants

@Composable
fun WebViewScreen(
    url: String,
    modifier: Modifier = Modifier
) {
    val state = rememberWebViewState(url)

    WebView(
        state = state,
        modifier = modifier.padding(horizontal = 20.dp)
    )
}

@Preview
@Composable
fun WebViewScreenPreview() {
    AppTheme {
        WebViewScreen(
            url = Constants.IMPRINT_URI,
            modifier = Modifier.fillMaxSize()
        )
    }
}
