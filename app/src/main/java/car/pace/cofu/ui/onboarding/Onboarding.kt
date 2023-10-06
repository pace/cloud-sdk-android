package car.pace.cofu.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import car.pace.cofu.ui.AppScaffold

@Composable
fun Onboarding() {
    AppScaffold { innerPadding ->
        OnboardingContent(modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun OnboardingContent(modifier: Modifier) {
    // TODO: Onboarding content
    Column(modifier = modifier) {
        Text(text = "Onboarding content")
    }
}
