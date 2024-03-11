package car.pace.cofu.ui.more.legal

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.ui.component.TextTopBar
import car.pace.cofu.ui.component.WebView
import car.pace.cofu.ui.component.rememberWebViewState
import car.pace.cofu.ui.consent.Consent

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LegalDocumentScreen(
    viewModel: LegalDocumentViewModel = hiltViewModel(),
    legalConsent: Consent.Legal,
    onNavigateUp: () -> Unit
) {
    Column {
        val context = LocalContext.current
        val url = remember {
            viewModel.getUrl(legalConsent)
        }
        val client = remember {
            viewModel.createClient(context)
        }

        TextTopBar(
            onNavigateUp = onNavigateUp
        )

        WebView(
            state = rememberWebViewState(url),
            modifier = Modifier.fillMaxSize(),
            onCreated = { it.settings.javaScriptEnabled = true },
            client = client
        )
    }
}
