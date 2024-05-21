package cloud.pace.sdk.appkit.app.drawer.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.SwipeableDefaults.resistanceConfig
import androidx.compose.material.SwipeableState
import androidx.compose.material.Text
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cloud.pace.sdk.R
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.appkit.AppKit.defaultAppCallback
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.ui.theme.PACEBlue
import cloud.pace.sdk.ui.theme.PACETheme
import cloud.pace.sdk.ui.theme.Title
import cloud.pace.sdk.utils.toColorOrNull
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppDrawer(
    app: App,
    appCount: Int,
    callback: AppCallbackImpl = defaultAppCallback
) {
    val context = LocalContext.current

    AppDrawer(
        iconUrl = app.iconUrl,
        caption = app.description,
        distance = if (appCount > 1) app.distance else null,
        headline = app.name,
        iconBackgroundColor = app.iconBackgroundColor.toColorOrNull(),
        backgroundColor = app.textBackgroundColor.toColorOrNull(),
        textColor = app.textColor.toColorOrNull(),
        onClick = {
            AppKit.openAppActivity(context = context, app = app, callback = callback)
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppDrawer(
    iconUrl: String? = null,
    caption: String? = null,
    distance: Int? = null,
    headline: String? = null,
    iconBackgroundColor: Color? = null,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    initialDrawerValue: DrawerValue = DrawerValue.Closed,
    onClick: () -> Unit
) {
    val height = dimensionResource(id = R.dimen.app_drawer_height)

    BoxWithConstraints(
        modifier = Modifier
            .padding(start = 20.dp)
            .fillMaxWidth()
            .requiredHeight(height),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Disable elevation overlay in Card so that the background color is not lighter
        CompositionLocalProvider(LocalElevationOverlay provides null) {
            val coroutineScope = rememberCoroutineScope()
            val iconBoxSizePx = with(LocalDensity.current) { height.toPx() }
            val collapsedOffset = constraints.maxWidth - iconBoxSizePx
            val swipeableState = rememberSwipeableState(
                initialValue = initialDrawerValue,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 1000f)
            )
            val collapsed by remember {
                derivedStateOf {
                    swipeableState.targetValue == DrawerValue.Closed
                }
            }

            AppDrawerContent(
                modifier = Modifier.appDrawer(
                    swipeableState = swipeableState,
                    collapsedOffset = collapsedOffset,
                    onClick = onClick
                ),
                iconUrl = iconUrl,
                caption = caption,
                distance = distance,
                headline = headline,
                isExpanded = !collapsed,
                iconBackgroundColor = iconBackgroundColor,
                backgroundColor = backgroundColor,
                textColor = textColor
            ) {
                coroutineScope.launch {
                    swipeableState.animateTo(DrawerValue.Closed)
                }
            }
        }
    }
}

@Composable
fun AppDrawerContent(
    modifier: Modifier = Modifier,
    height: Dp = dimensionResource(id = R.dimen.app_drawer_height),
    isExpanded: Boolean,
    iconUrl: String? = null,
    caption: String? = null,
    distance: Int? = null,
    headline: String? = null,
    iconBackgroundColor: Color? = null,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    onClose: () -> Unit
) {
    val drawerIconBackgroundColor = iconBackgroundColor ?: Title
    val drawerBackgroundColor = backgroundColor ?: PACEBlue
    val drawerTextColor = textColor ?: Title

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(
            topStartPercent = 50,
            bottomStartPercent = 50
        ),
        backgroundColor = drawerBackgroundColor,
        elevation = 10.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val endCornerRadius by animateIntAsState(targetValue = if (isExpanded) 50 else 0, label = "End corner radius animation")

            AppDrawerIcon(
                size = height,
                backgroundColor = drawerIconBackgroundColor,
                endCornerRadius = endCornerRadius,
                iconUrl = iconUrl
            )

            AppDrawerText(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                caption = caption,
                headline = headline,
                distance = distance,
                textColor = drawerTextColor,
                backgroundColor = drawerBackgroundColor
            )

            AppDrawerCloseButton(
                modifier = Modifier.padding(horizontal = 5.dp),
                tint = drawerTextColor,
                onClick = onClose
            )
        }
    }
}

@Composable
fun AppDrawerIcon(
    size: Dp,
    backgroundColor: Color,
    endCornerRadius: Int,
    iconUrl: String? = null
) {
    Box(
        modifier = Modifier
            .requiredSize(size)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(
                    topStartPercent = 50,
                    topEndPercent = endCornerRadius,
                    bottomEndPercent = endCornerRadius,
                    bottomStartPercent = 50
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = iconUrl ?: R.drawable.ic_cofu_blue,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            error = painterResource(id = R.drawable.ic_cofu_blue)
        )
    }
}

@Composable
fun AppDrawerText(
    modifier: Modifier = Modifier,
    caption: String? = null,
    headline: String? = null,
    distance: Int? = null,
    textColor: Color,
    backgroundColor: Color
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Row {
            Text(
                text = if (caption.isNullOrEmpty()) stringResource(id = R.string.default_drawer_first_line) else caption,
                modifier = Modifier.weight(weight = 1f, fill = false),
                color = textColor,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.caption
            )

            if (distance != null) {
                AppDrawerDistanceLabel(
                    modifier = Modifier.padding(start = 5.dp),
                    distance = distance,
                    textColor = backgroundColor,
                    backgroundColor = textColor
                )
            }
        }

        Text(
            text = if (headline.isNullOrEmpty()) stringResource(id = R.string.default_drawer_second_line) else headline,
            color = textColor,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.body1,
        )
    }
}

@Composable
fun AppDrawerDistanceLabel(
    modifier: Modifier = Modifier,
    distance: Int,
    textColor: Color,
    backgroundColor: Color
) {
    Row(
        modifier = modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_distance),
            contentDescription = null,
            tint = textColor
        )
        Text(
            text = "${if (distance <= 10) 0 else distance} m",
            modifier = Modifier.padding(start = 3.dp),
            color = textColor,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.overline
        )
    }
}

@Composable
fun AppDrawerCloseButton(
    modifier: Modifier = Modifier,
    tint: Color,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_close),
            contentDescription = null,
            tint = tint
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Modifier.appDrawer(
    swipeableState: SwipeableState<DrawerValue>,
    collapsedOffset: Float,
    onClick: () -> Unit
): Modifier {
    val coroutineScope = rememberCoroutineScope()
    val anchors = mapOf(0f to DrawerValue.Open, collapsedOffset to DrawerValue.Closed)
    val collapsed by remember {
        derivedStateOf {
            swipeableState.targetValue == DrawerValue.Closed
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            swipeableState.animateTo(
                targetValue = DrawerValue.Open,
                anim = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 1000f)
            )
        }
    }

    return this then Modifier
        .offset {
            IntOffset(swipeableState.offset.value.roundToInt(), 0)
        }
        .swipeable(
            state = swipeableState,
            anchors = anchors,
            orientation = Orientation.Horizontal,
            thresholds = { _, _ -> FractionalThreshold(0.3f) },
            resistance = resistanceConfig(
                anchors = anchors.keys,
                factorAtMin = 80f,
                factorAtMax = SwipeableDefaults.StandardResistanceFactor
            )
        )
        .clickable {
            if (collapsed) {
                coroutineScope.launch {
                    swipeableState.animateTo(DrawerValue.Open)
                }
            } else {
                onClick()
            }
        }
}

@Preview(showBackground = true)
@Composable
fun AppDrawerPreview() {
    PACETheme {
        AppDrawer(
            distance = 270,
            initialDrawerValue = DrawerValue.Open
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
fun AppDrawerIconPreview() {
    PACETheme {
        AppDrawerIcon(
            size = 64.dp,
            backgroundColor = Title,
            endCornerRadius = 50
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppDrawerTextPreview() {
    PACETheme {
        AppDrawerText(
            distance = 270,
            textColor = Title,
            backgroundColor = PACEBlue
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppDrawerDistanceLabelPreview() {
    PACETheme {
        AppDrawerDistanceLabel(
            distance = 270,
            textColor = PACEBlue,
            backgroundColor = Title
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppDrawerCloseButtonPreview() {
    PACETheme {
        AppDrawerCloseButton(tint = Title) {}
    }
}
