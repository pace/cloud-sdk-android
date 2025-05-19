package car.pace.cofu.ui.onboarding.legal

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.R
import car.pace.cofu.ui.Route
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun LegalPage(
    viewModel: LegalViewModel = hiltViewModel(),
    onNavigate: (Route) -> Unit,
    onNext: () -> Unit
) {
    PageScaffold(
        imageVector = Icons.Outlined.Balance,
        titleRes = R.string.onboarding_legal_title,
        nextButtonTextRes = R.string.common_use_accept,
        onNextButtonClick = {
            viewModel.acceptTermsAndPrivacy()
            onNext()
        },
        descriptionContent = {
            val annotatedText = buildAnnotatedString {
                val termsText = stringResource(id = R.string.onboarding_legal_terms_of_use)
                val privacyText = stringResource(id = R.string.onboarding_legal_data_privacy)
                val fullText = stringResource(id = R.string.onboarding_legal_description)

                val termStartIndex = fullText.indexOf(termsText)
                val termEndIndex = termStartIndex + termsText.length
                val privacyStartIndex = fullText.indexOf(privacyText)
                val privacyEndIndex = privacyStartIndex + privacyText.length

                append(fullText)

                addStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    start = termStartIndex,
                    end = termEndIndex
                )

                addLink(
                    clickable = LinkAnnotation.Clickable(
                        tag = Route.ONBOARDING_TERMS.name,
                        linkInteractionListener = {
                            onNavigate(Route.ONBOARDING_TERMS)
                        }
                    ),
                    start = termStartIndex,
                    end = termEndIndex
                )

                addStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    start = privacyStartIndex,
                    end = privacyEndIndex
                )

                addLink(
                    clickable = LinkAnnotation.Clickable(
                        tag = Route.ONBOARDING_PRIVACY.name,
                        linkInteractionListener = {
                            onNavigate(Route.ONBOARDING_PRIVACY)
                        }
                    ),
                    start = privacyStartIndex,
                    end = privacyEndIndex
                )
            }

            Text(
                text = annotatedText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
            )
        }
    )
}

@Preview
@Composable
fun LegalPagePreview() {
    AppTheme {
        LegalPage(
            onNavigate = {},
            onNext = {}
        )
    }
}
