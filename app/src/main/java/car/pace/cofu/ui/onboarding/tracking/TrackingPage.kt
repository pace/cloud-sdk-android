package car.pace.cofu.ui.onboarding.tracking

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ScreenLockPortrait
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.R
import car.pace.cofu.ui.component.SecondaryButton
import car.pace.cofu.ui.navigation.graph.Route
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun TrackingPage(
    viewModel: TrackingPageViewModel = hiltViewModel(),
    onNavigate: (Route) -> Unit,
    onNext: () -> Unit
) {
    PageScaffold(
        imageVector = Icons.Outlined.ScreenLockPortrait,
        titleRes = R.string.onboarding_tracking_title,
        nextButtonTextRes = R.string.common_use_accept,
        onNextButtonClick = {
            viewModel.enableAnalytics()
            onNext()
        },
        descriptionContent = {
            val annotatedText = buildAnnotatedString {
                val trackingText = stringResource(id = R.string.onboarding_tracking_app_tracking)
                val fullText = stringResource(id = R.string.onboarding_tracking_description)

                val trackingStartIndex = fullText.indexOf(trackingText)
                val trackingEndIndex = trackingStartIndex + trackingText.length

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
                    tag = Route.ANALYSIS.name,
                    annotation = Route.ANALYSIS.name,
                    start = trackingStartIndex,
                    end = trackingEndIndex
                )
            }

            ClickableText(
                text = annotatedText,
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
        },
        footerContent = {
            SecondaryButton(
                text = stringResource(id = R.string.common_use_decline),
                modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp),
                onClick = {
                    viewModel.disableAnalytics()
                    onNext()
                }
            )
        }
    )
}

@Preview
@Composable
fun TrackingPagePreview() {
    AppTheme {
        TrackingPage(
            onNavigate = {},
            onNext = {}
        )
    }
}
