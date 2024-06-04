package car.pace.cofu.ui.consent

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.R
import car.pace.cofu.data.PermissionRepository.Companion.NOTIFICATION_PERMISSION
import car.pace.cofu.ui.Route
import car.pace.cofu.ui.component.ClickableText
import car.pace.cofu.ui.component.Description
import car.pace.cofu.ui.component.PrimaryButton
import car.pace.cofu.ui.component.SecondaryButton
import car.pace.cofu.ui.component.Title
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.LogAndBreadcrumb

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConsentScreen(
    viewModel: ConsentViewModel = hiltViewModel(),
    onNavigate: (Route) -> Unit,
    onDone: () -> Unit
) {
    val pageIndex = viewModel.pageIndex
    val pagerState = rememberPagerState { viewModel.getCountOfPages() }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        LogAndBreadcrumb.i(LogAndBreadcrumb.CONSENT, "Manifest.permission.POST_NOTIFICATIONS ${if (it) "is granted" else "is not granted"}")
        viewModel.nextPage()
    }

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
            Consent.Document.Terms -> TermsConsentPage(onNavigate = onNavigate, onAccept = viewModel::acceptTerms)
            Consent.Document.Privacy -> PrivacyConsentPage(onNavigate = onNavigate, onAccept = viewModel::acceptPrivacy)
            Consent.Document.Tracking -> TrackingConsentPage(onNavigate = onNavigate, onDecline = viewModel::declineTracking, onAccept = viewModel::acceptTracking)
            Consent.Notification -> NotificationConsentPage {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    viewModel.notificationPermissionRequested()
                    launcher.launch(NOTIFICATION_PERMISSION)
                }
            }

            else -> {}
        }
    }
}

@Composable
fun TermsConsentPage(
    onNavigate: (Route) -> Unit,
    onAccept: () -> Unit
) {
    LegalConsentScaffold(
        title = stringResource(id = R.string.legal_update_terms_title),
        linkText = stringResource(id = R.string.onboarding_legal_terms_of_use),
        fullText = stringResource(id = R.string.legal_update_terms_description),
        linkTextRoute = Route.ONBOARDING_TERMS,
        onNavigate = onNavigate,
        onAccept = onAccept
    )
}

@Composable
fun PrivacyConsentPage(
    onNavigate: (Route) -> Unit,
    onAccept: () -> Unit
) {
    LegalConsentScaffold(
        title = stringResource(id = R.string.legal_update_privacy_title),
        linkText = stringResource(id = R.string.onboarding_legal_data_privacy),
        fullText = stringResource(id = R.string.legal_update_privacy_description),
        linkTextRoute = Route.ONBOARDING_PRIVACY,
        onNavigate = onNavigate,
        onAccept = onAccept
    )
}

@Composable
fun TrackingConsentPage(
    onNavigate: (Route) -> Unit,
    onDecline: () -> Unit,
    onAccept: () -> Unit
) {
    LegalConsentScaffold(
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
fun LegalConsentScaffold(
    title: String,
    linkText: String,
    fullText: String,
    linkTextRoute: Route,
    canBeDeclined: Boolean = false,
    onNavigate: (Route) -> Unit,
    onDecline: () -> Unit = {},
    onAccept: () -> Unit
) {
    ConsentScaffold(
        title = title,
        description = {
            ClickableText(
                linkText = linkText,
                fullText = fullText,
                linkTextRoute = linkTextRoute,
                onNavigate = onNavigate
            )
        },
        canBeDeclined = canBeDeclined,
        onDecline = onDecline,
        onAccept = onAccept
    )
}

@Composable
fun NotificationConsentPage(
    onNext: () -> Unit
) {
    ConsentScaffold(
        title = stringResource(id = R.string.notification_permission_request_title),
        description = {
            Description(text = stringResource(id = R.string.onboarding_notification_permission_description))
        },
        acceptText = R.string.common_use_next,
        onAccept = onNext
    )
}

@Composable
fun ConsentScaffold(
    title: String,
    icon: ImageVector = Icons.Outlined.Info,
    description: @Composable () -> Unit,
    canBeDeclined: Boolean = false,
    @StringRes acceptText: Int = R.string.common_use_accept,
    @StringRes declineText: Int = R.string.common_use_decline,
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
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Title(
                text = title,
                modifier = Modifier.padding(top = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            description()

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (canBeDeclined) {
            SecondaryButton(
                text = stringResource(id = declineText),
                modifier = Modifier.padding(top = 12.dp),
                onClick = onDecline
            )
        }

        PrimaryButton(
            text = stringResource(id = acceptText),
            modifier = Modifier.padding(top = 12.dp, bottom = 28.dp),
            onClick = onAccept
        )
    }
}

@Preview
@Composable
fun TermsConsentPagePreview() {
    AppTheme {
        TermsConsentPage(
            onNavigate = {},
            onAccept = {}
        )
    }
}

@Preview
@Composable
fun PrivacyConsentPagePreview() {
    AppTheme {
        PrivacyConsentPage(
            onNavigate = {},
            onAccept = {}
        )
    }
}

@Preview
@Composable
fun TrackingConsentPagePreview() {
    AppTheme {
        TrackingConsentPage(
            onNavigate = {},
            onDecline = {},
            onAccept = {}
        )
    }
}

@Preview
@Composable
fun NotificationConsentPagePreview() {
    AppTheme {
        NotificationConsentPage {}
    }
}
