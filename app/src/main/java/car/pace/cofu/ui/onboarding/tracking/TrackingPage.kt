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
import car.pace.cofu.R
import car.pace.cofu.ui.component.SecondaryButton
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.LegalDocument
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

@Composable
fun TrackingPage(
    onLegalDocument: (LegalDocument) -> Unit,
    onNext: () -> Unit
) {
    PageScaffold(
        imageVector = Icons.Outlined.ScreenLockPortrait,
        titleRes = R.string.onboarding_tracking_title,
        nextButtonTextRes = R.string.common_use_accept,
        onNextButtonClick = {
            Firebase.analytics.setAnalyticsCollectionEnabled(true)
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
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    start = trackingStartIndex,
                    end = trackingEndIndex
                )

                addStringAnnotation(
                    tag = LegalDocument.TRACKING.name,
                    annotation = LegalDocument.TRACKING.name,
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
                        val document = LegalDocument.valueOf(annotation.item)
                        onLegalDocument(document)
                    }
            }
        },
        footerContent = {
            SecondaryButton(
                text = stringResource(id = R.string.common_use_decline),
                modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp),
                onClick = {
                    Firebase.analytics.setAnalyticsCollectionEnabled(false)
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
            onLegalDocument = {},
            onNext = {}
        )
    }
}
