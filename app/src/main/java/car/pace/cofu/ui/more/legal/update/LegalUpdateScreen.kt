package car.pace.cofu.ui.more.legal.update

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.R
import car.pace.cofu.ui.component.ClickableText
import car.pace.cofu.ui.component.PrimaryButton
import car.pace.cofu.ui.component.SecondaryButton
import car.pace.cofu.ui.component.Title
import car.pace.cofu.ui.navigation.graph.Route
import car.pace.cofu.ui.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LegalUpdateScreen(
    viewModel: LegalUpdateViewModel = hiltViewModel(),
    onNavigate: (Route) -> Unit,
    onDone: () -> Unit
) {
    val pagerState = rememberPagerState { viewModel.getCountOfPages() }
    val pageIndex = viewModel.pageIndex

    LaunchedEffect(pageIndex) {
        if (pageIndex >= viewModel.getCountOfPages()) {
            onDone()
        } else {
            pagerState.animateScrollToPage(pageIndex)
        }
    }

    HorizontalPager(
        state = pagerState,
        userScrollEnabled = false
    ) {
        when (viewModel.getPage(it)) {
            LegalDocument.TERMS -> TermsUpdatePage(onNavigate = onNavigate, onAccept = viewModel::acceptTerms)
            LegalDocument.PRIVACY -> PrivacyUpdatePage(onNavigate = onNavigate, onAccept = viewModel::acceptPrivacy)
            LegalDocument.TRACKING -> TrackingUpdatePage(onNavigate = onNavigate, onDecline = viewModel::declineTracking, onAccept = viewModel::acceptTracking)
            else -> {}
        }
    }
}

@Composable
fun TermsUpdatePage(
    onNavigate: (Route) -> Unit,
    onAccept: () -> Unit
) {
    LegalUpdateScreenContent(
        title = stringResource(id = R.string.legal_update_terms_title),
        linkText = stringResource(id = R.string.onboarding_legal_terms_of_use),
        fullText = stringResource(id = R.string.legal_update_terms_description),
        linkTextRoute = Route.ONBOARDING_TERMS,
        onNavigate = onNavigate,
        onAccept = onAccept
    )
}

@Composable
fun PrivacyUpdatePage(
    onNavigate: (Route) -> Unit,
    onAccept: () -> Unit
) {
    LegalUpdateScreenContent(
        title = stringResource(id = R.string.legal_update_privacy_title),
        linkText = stringResource(id = R.string.onboarding_legal_data_privacy),
        fullText = stringResource(id = R.string.legal_update_privacy_description),
        linkTextRoute = Route.ONBOARDING_PRIVACY,
        onNavigate = onNavigate,
        onAccept = onAccept
    )
}

@Composable
fun TrackingUpdatePage(
    onNavigate: (Route) -> Unit,
    onDecline: () -> Unit,
    onAccept: () -> Unit
) {
    LegalUpdateScreenContent(
        title = stringResource(id = R.string.legal_update_tracking_title),
        linkText = stringResource(id = R.string.onboarding_tracking_app_tracking),
        fullText = stringResource(id = R.string.legal_update_tracking_description),
        linkTextRoute = Route.ONBOARDING_ANALYSIS,
        canBeDeclined = true,
        onNavigate = onNavigate,
        onDecline = onDecline,
        onAccept = onAccept
    )
}

@Composable
fun LegalUpdateScreenContent(
    title: String,
    linkText: String,
    fullText: String,
    linkTextRoute: Route,
    canBeDeclined: Boolean = false,
    onNavigate: (Route) -> Unit,
    onDecline: () -> Unit = {},
    onAccept: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp

            Spacer(
                modifier = Modifier.height((screenHeight * 0.33).dp)
            )

            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Title(
                text = title,
                modifier = Modifier.padding(top = 20.dp)
            )

            ClickableText(
                linkText = linkText,
                fullText = fullText,
                linkTextRoute = linkTextRoute,
                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp),
                onNavigate = onNavigate
            )
        }

        if (canBeDeclined) {
            SecondaryButton(
                text = stringResource(id = R.string.common_use_decline),
                modifier = Modifier.padding(top = 12.dp),
                onClick = onDecline
            )
        }

        PrimaryButton(
            text = stringResource(id = R.string.common_use_accept),
            modifier = Modifier.padding(top = 12.dp, bottom = 28.dp),
            onClick = onAccept
        )
    }
}

@Preview
@Composable
fun TermsUpdatePagePreview() {
    AppTheme {
        TermsUpdatePage(
            onNavigate = {},
            onAccept = {}
        )
    }
}

@Preview
@Composable
fun PrivacyUpdatePagePreview() {
    AppTheme {
        PrivacyUpdatePage(
            onNavigate = {},
            onAccept = {}
        )
    }
}

@Preview
@Composable
fun TrackingUpdatePagePreview() {
    AppTheme {
        TrackingUpdatePage(
            onNavigate = {},
            onDecline = {},
            onAccept = {}
        )
    }
}
