package car.pace.cofu.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun DefaultCircularProgressIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier.size(100.dp),
        strokeCap = StrokeCap.Round
    )
}

@Composable
fun DefaultLinearProgressIndicator(modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        modifier = modifier.fillMaxWidth(),
        trackColor = Color.Transparent,
        strokeCap = StrokeCap.Round
    )
}

@Preview
@Composable
fun DefaultCircularProgressIndicatorPreview() {
    AppTheme {
        DefaultCircularProgressIndicator()
    }
}

@Preview
@Composable
fun DefaultLinearProgressIndicatorPreview() {
    AppTheme {
        DefaultLinearProgressIndicator()
    }
}
