package car.pace.cofu.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import car.pace.cofu.R
import car.pace.cofu.ui.Route
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
        color = MaterialTheme.colorScheme.onPrimary,
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
        color = MaterialTheme.colorScheme.onPrimary,
        textAlign = textAlign,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
fun ClickableText(
    linkText: String,
    fullText: String,
    linkTextRoute: Route,
    modifier: Modifier = Modifier,
    onNavigate: (Route) -> Unit
) {
    val annotatedText = buildAnnotatedString {
        val trackingStartIndex = fullText.indexOf(linkText)
        val trackingEndIndex = trackingStartIndex + linkText.length

        append(fullText)

        addStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            ),
            start = trackingStartIndex,
            end = trackingEndIndex
        )

        addStringAnnotation(
            tag = linkTextRoute.name,
            annotation = linkTextRoute.name,
            start = trackingStartIndex,
            end = trackingEndIndex
        )
    }

    ClickableText(
        annotatedText = annotatedText,
        modifier = modifier,
        onNavigate = onNavigate
    )
}

@Composable
fun ClickableText(
    annotatedText: AnnotatedString,
    modifier: Modifier = Modifier,
    onNavigate: (Route) -> Unit
) {
    androidx.compose.foundation.text.ClickableText(
        text = annotatedText,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center
        )
    ) {
        annotatedText
            .getStringAnnotations(start = it, end = it)
            .firstOrNull()?.let { annotation ->
                val route = Route.valueOf(annotation.item)
                onNavigate(route)
            }
    }
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

@Preview
@Composable
fun TrackingDescriptionPreview() {
    AppTheme {
        ClickableText(
            linkText = stringResource(id = R.string.onboarding_tracking_app_tracking),
            fullText = stringResource(id = R.string.onboarding_tracking_description),
            linkTextRoute = Route.ONBOARDING_ANALYSIS,
            onNavigate = {}
        )
    }
}
