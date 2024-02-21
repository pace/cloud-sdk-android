package car.pace.cofu.ui.more.tracking

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import car.pace.cofu.R
import car.pace.cofu.ui.component.ClickableText
import car.pace.cofu.ui.component.PrimaryButton
import car.pace.cofu.ui.component.SecondaryButton
import car.pace.cofu.ui.component.TextTopBar
import car.pace.cofu.ui.component.Title
import car.pace.cofu.ui.icon.BarChart4Bars
import car.pace.cofu.ui.navigation.graph.Route
import car.pace.cofu.ui.onboarding.tracking.TrackingViewModel
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.theme.Success
import car.pace.cofu.util.Constants.FADE_DURATION
import car.pace.cofu.util.Constants.SPACER_CONTENT_TYPE
import car.pace.cofu.util.Constants.SPACER_KEY
import car.pace.cofu.util.Constants.TITLE_CONTENT_TYPE
import car.pace.cofu.util.Constants.TRACKING_BADGE_CONTENT_TYPE
import car.pace.cofu.util.Constants.TRACKING_BADGE_KEY
import car.pace.cofu.util.Constants.TRACKING_DESCRIPTION_CONTENT_TYPE
import car.pace.cofu.util.Constants.TRACKING_DESCRIPTION_KEY
import car.pace.cofu.util.Constants.TRACKING_ICON_CONTENT_TYPE
import car.pace.cofu.util.Constants.TRACKING_ICON_KEY
import car.pace.cofu.util.Constants.TRACKING_TITLE_KEY
import car.pace.cofu.util.LogAndBreadcrumb

@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel = hiltViewModel(),
    onNavigate: (Route) -> Unit,
    onNavigateUp: () -> Unit
) {
    val trackingEnabled by viewModel.trackingEnabled.collectAsStateWithLifecycle()

    TrackingScreenContent(
        trackingEnabled = trackingEnabled,
        enableAnalytics = { viewModel.enableAnalytics(LogAndBreadcrumb.TRACKING) },
        disableAnalytics = { viewModel.disableAnalytics(LogAndBreadcrumb.TRACKING) },
        onNavigate = onNavigate,
        onNavigateUp = onNavigateUp
    )
}

@Composable
fun TrackingScreenContent(
    trackingEnabled: Boolean,
    enableAnalytics: () -> Unit,
    disableAnalytics: () -> Unit,
    onNavigate: (Route) -> Unit,
    onNavigateUp: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val transition = updateTransition(targetState = trackingEnabled, label = "trackingEnabledTransition")
        val buttonModifier = Modifier.padding(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 12.dp)

        TextTopBar(
            text = stringResource(id = R.string.menu_items_analytics),
            onNavigateUp = onNavigateUp
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(
                key = SPACER_KEY,
                contentType = SPACER_CONTENT_TYPE
            ) {
                Spacer(
                    modifier = Modifier.fillParentMaxHeight(0.15f)
                )
            }

            item(
                key = TRACKING_BADGE_KEY,
                contentType = TRACKING_BADGE_CONTENT_TYPE
            ) {
                val color by transition.animateColor(
                    transitionSpec = { tween(FADE_DURATION) },
                    label = "badgeColor"
                ) {
                    if (it) Success else MaterialTheme.colorScheme.error
                }

                Row(
                    modifier = Modifier
                        .drawBehind {
                            drawRoundRect(color = color, cornerRadius = CornerRadius(52.dp.toPx()))
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (trackingEnabled) Icons.Outlined.CheckCircle else Icons.Outlined.Block,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.background
                    )

                    Text(
                        text = stringResource(id = if (trackingEnabled) R.string.analytics_accepted_text else R.string.analytics_declined_text),
                        modifier = Modifier.padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.background,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            item(
                key = TRACKING_ICON_KEY,
                contentType = TRACKING_ICON_CONTENT_TYPE
            ) {
                Icon(
                    imageVector = Icons.Outlined.BarChart4Bars,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 20.dp, top = 20.dp, end = 20.dp)
                        .size(182.dp),
                    tint = MaterialTheme.colorScheme.surface
                )
            }

            item(
                key = TRACKING_TITLE_KEY,
                contentType = TITLE_CONTENT_TYPE
            ) {
                Title(
                    text = stringResource(id = R.string.onboarding_tracking_title),
                    modifier = Modifier.padding(top = 28.dp)
                )
            }

            item(
                key = TRACKING_DESCRIPTION_KEY,
                contentType = TRACKING_DESCRIPTION_CONTENT_TYPE
            ) {
                ClickableText(
                    linkText = stringResource(id = R.string.onboarding_tracking_app_tracking),
                    fullText = stringResource(id = R.string.onboarding_tracking_description),
                    linkTextRoute = Route.ANALYSIS,
                    modifier = Modifier.padding(top = 20.dp, bottom = 12.dp),
                    onNavigate = onNavigate
                )
            }
        }

        transition.AnimatedContent {
            if (it) {
                SecondaryButton(
                    text = stringResource(id = R.string.common_use_decline),
                    modifier = buttonModifier,
                    onClick = disableAnalytics
                )
            } else {
                PrimaryButton(
                    text = stringResource(R.string.common_use_accept),
                    modifier = buttonModifier,
                    onClick = enableAnalytics
                )
            }
        }
    }
}

@Preview
@Composable
fun TrackingEnabledScreenPreview() {
    TrackingScreenPreview(trackingEnabled = true)
}

@Preview
@Composable
fun TrackingDisabledScreenPreview() {
    TrackingScreenPreview(trackingEnabled = false)
}

@Composable
private fun TrackingScreenPreview(
    trackingEnabled: Boolean
) {
    AppTheme {
        TrackingScreenContent(
            trackingEnabled = trackingEnabled,
            enableAnalytics = {},
            disableAnalytics = {},
            onNavigate = {},
            onNavigateUp = {}
        )
    }
}
