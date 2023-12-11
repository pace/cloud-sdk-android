package car.pace.cofu.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun DefaultCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = color,
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
