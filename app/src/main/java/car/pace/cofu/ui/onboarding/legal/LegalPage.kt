package car.pace.cofu.ui.onboarding.legal

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import car.pace.cofu.R
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.LegalDocument

@Composable
fun LegalPage(
    onLegalDocument: (LegalDocument) -> Unit,
    onNext: () -> Unit
) {
    PageScaffold(
        imageVector = Icons.Outlined.Balance,
        titleRes = R.string.onboarding_legal_title,
        nextButtonTextRes = R.string.common_use_accept,
        onNextButtonClick = onNext,
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
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    start = termStartIndex,
                    end = termEndIndex
                )

                addStringAnnotation(
                    tag = LegalDocument.TERMS.name,
                    annotation = LegalDocument.TERMS.name,
                    start = termStartIndex,
                    end = termEndIndex
                )

                addStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    start = privacyStartIndex,
                    end = privacyEndIndex
                )

                addStringAnnotation(
                    tag = LegalDocument.PRIVACY.name,
                    annotation = LegalDocument.PRIVACY.name,
                    start = privacyStartIndex,
                    end = privacyEndIndex
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
        }
    )
}

@Preview
@Composable
fun LegalPagePreview() {
    AppTheme {
        LegalPage(
            onLegalDocument = {},
            onNext = {}
        )
    }
}
