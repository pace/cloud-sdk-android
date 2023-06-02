package cloud.pace.sdk.appkit.app.drawer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cloud.pace.sdk.R
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.appkit.AppKit.defaultAppCallback
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import cloud.pace.sdk.ui.theme.PACETheme
import cloud.pace.sdk.utils.KoinConfig
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import timber.log.Timber

@Composable
fun AppDrawerHost(
    callback: AppCallbackImpl = defaultAppCallback
) {
    AppDrawerHost(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 30.dp),
        callback = callback
    )
}

@OptIn(KoinInternalApi::class)
@Composable
fun AppDrawerHost(
    modifier: Modifier = Modifier,
    callback: AppCallbackImpl = defaultAppCallback
) {
    PACETheme {
        CompositionLocalProvider(
            LocalKoinApplication provides KoinConfig.cloudSDKKoinApp.koin,
            LocalKoinScope provides KoinConfig.cloudSDKKoinApp.koin.scopeRegistry.rootScope
        ) {
            val viewModel = koinViewModel<AppDrawerViewModel>()
            val context = LocalContext.current

            BoxWithConstraints(
                modifier = modifier,
                contentAlignment = Alignment.BottomEnd
            ) {
                val space = dimensionResource(id = R.dimen.app_drawer_spacing)
                val drawerHeight = dimensionResource(id = R.dimen.app_drawer_height)
                val size = remember(maxHeight) {
                    // Calculate number of elements that fit into max height
                    maxHeight.div(space + drawerHeight).toInt().coerceAtLeast(0)
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        space = space,
                        alignment = Alignment.Bottom
                    ),
                    horizontalAlignment = Alignment.End
                ) {
                    val apps by viewModel.apps.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
                    val maxApps = apps.take(size)

                    maxApps.forEach {
                        AppDrawer(
                            iconUrl = it.iconUrl,
                            caption = it.description,
                            distance = if (apps.size > 1) it.distance else null,
                            headline = it.name,
                            iconBackgroundColor = it.iconBackgroundColor.toColorOrNull(),
                            backgroundColor = it.textBackgroundColor.toColorOrNull(),
                            textColor = it.textColor.toColorOrNull(),
                            onClick = {
                                AppKit.openAppActivity(context = context, app = it, callback = callback)
                            }
                        )
                    }

                    LaunchedEffect(maxApps, callback) {
                        callback.onShow(maxApps)
                    }
                }
            }
        }
    }
}

private fun String?.toColorOrNull(): Color? {
    this ?: return null

    return try {
        val colorInt = android.graphics.Color.parseColor(this)
        Color(colorInt)
    } catch (e: Exception) {
        Timber.e(e, "Could not parse $this to color")
        null
    }
}
