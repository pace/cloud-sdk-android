package car.pace.cofu.ui.onboarding.tracking

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.R
import car.pace.cofu.ui.Route
import car.pace.cofu.ui.component.ClickableText
import car.pace.cofu.ui.component.SecondaryButton
import car.pace.cofu.ui.icon.BarChart4Bars
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.LogAndBreadcrumb

@Composable
fun TrackingPage(
    viewModel: TrackingViewModel = hiltViewModel(),
    onNavigate: (Route) -> Unit,
    onNext: () -> Unit
) {
    PageScaffold(
        imageVector = Icons.Outlined.BarChart4Bars,
        titleRes = R.string.onboarding_tracking_title,
        nextButtonTextRes = R.string.common_use_accept,
        onNextButtonClick = {
            viewModel.enableAnalytics(LogAndBreadcrumb.ONBOARDING)
            onNext()
        },
        descriptionContent = {
            ClickableText(
                linkText = stringResource(id = R.string.onboarding_tracking_app_tracking),
                fullText = stringResource(id = R.string.onboarding_tracking_description),
                linkTextRoute = Route.ONBOARDING_ANALYSIS,
                onNavigate = onNavigate
            )
        },
        footerContent = {
            SecondaryButton(
                text = stringResource(id = R.string.common_use_decline),
                modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp),
                onClick = {
                    viewModel.disableAnalytics(LogAndBreadcrumb.ONBOARDING)
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
