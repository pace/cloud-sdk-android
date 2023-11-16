package car.pace.cofu.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun Title(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center
) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = textAlign,
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
fun Description(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center
) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = textAlign,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Preview
@Composable
fun TitlePreview() {
    AppTheme {
        Title(text = "This is a title")
    }
}

@Preview
@Composable
fun DescriptionPreview() {
    AppTheme {
        Description(text = "This is a description with two lines because there is a lot of text.")
    }
}
