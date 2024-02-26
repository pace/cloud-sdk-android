package car.pace.cofu.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.R
import car.pace.cofu.data.PermissionRepository.Companion.locationPermissions
import car.pace.cofu.util.Constants.FADE_DURATION
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.clustering.rememberClusterManager
import com.google.maps.android.compose.clustering.rememberClusterRenderer
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun LoadingMap(
    @DrawableRes loadingImage: Int,
    mapLoading: Boolean,
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    gesturesEnabled: Boolean = true,
    liteModeEnabled: Boolean = false,
    isMyLocationEnabled: Boolean = true,
    onMapClick: ((LatLng) -> Unit)? = null,
    onMapLoaded: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable @GoogleMapComposable
    () -> Unit
) {
    Box(modifier = modifier) {
        Map(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            gesturesEnabled = gesturesEnabled,
            liteModeEnabled = liteModeEnabled,
            isMyLocationEnabled = isMyLocationEnabled,
            onMapClick = onMapClick,
            onMapLoaded = onMapLoaded,
            contentPadding = contentPadding,
            content = content
        )

        AnimatedVisibility(
            visible = mapLoading,
            enter = fadeIn(animationSpec = tween(FADE_DURATION)),
            exit = fadeOut(animationSpec = tween(FADE_DURATION))
        ) {
            Image(
                painter = painterResource(id = loadingImage),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Map(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    gesturesEnabled: Boolean = true,
    liteModeEnabled: Boolean = false,
    isMyLocationEnabled: Boolean = true,
    onMapClick: ((LatLng) -> Unit)? = null,
    onMapLoaded: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable @GoogleMapComposable
    () -> Unit
) {
    // When in preview, early return a Box with the received modifier preserving layout
    if (LocalInspectionMode.current) {
        Box(modifier = modifier)
        return
    }

    val context = LocalContext.current
    val backgroundColor = MaterialTheme.colorScheme.surface
    val googleMapOptionsFactory = remember {
        GoogleMapOptions()
            .backgroundColor(backgroundColor.toArgb())
            .liteMode(liteModeEnabled)
    }
    val permissionState = rememberMultiplePermissionsState(permissions = locationPermissions)
    val permissionGranted = permissionState.permissions.any { it.status.isGranted }
    val properties = remember(isMyLocationEnabled, permissionGranted) {
        // Location permission must be granted to display the my location marker, otherwise the app crashes
        MapProperties(
            isMyLocationEnabled = isMyLocationEnabled && permissionGranted,
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
        )
    }
    val uiSettings = remember {
        MapUiSettings(
            compassEnabled = gesturesEnabled,
            indoorLevelPickerEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            rotationGesturesEnabled = gesturesEnabled,
            scrollGesturesEnabled = gesturesEnabled,
            scrollGesturesEnabledDuringRotateOrZoom = gesturesEnabled,
            tiltGesturesEnabled = gesturesEnabled,
            zoomControlsEnabled = false,
            zoomGesturesEnabled = gesturesEnabled
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        googleMapOptionsFactory = { googleMapOptionsFactory },
        properties = properties,
        uiSettings = uiSettings,
        onMapClick = onMapClick,
        onMapLoaded = onMapLoaded,
        contentPadding = contentPadding,
        content = content
    )
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun <T : ClusterItem> NonHierarchicalClustering(
    items: Collection<T>,
    onClusterItemClick: (T) -> Boolean = { false },
    clusterContent: @Composable ((Cluster<T>) -> Unit)? = null,
    clusterItemContent: @Composable ((T) -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val algorithm = remember(screenWidth, screenHeight) {
        NonHierarchicalViewBasedAlgorithm<T>(
            screenWidth.value.toInt(),
            screenHeight.value.toInt()
        )
    }

    val clusterManager = rememberClusterManager<T>()
    val renderer = rememberClusterRenderer(
        clusterContent = clusterContent,
        clusterItemContent = clusterItemContent,
        clusterManager = clusterManager
    )

    SideEffect {
        clusterManager ?: return@SideEffect
        clusterManager.setOnClusterItemClickListener(onClusterItemClick)
    }

    SideEffect {
        // Here the clusterManager is being customized with a NonHierarchicalViewBasedAlgorithm.
        // This speeds up by a factor the rendering of items on the screen.
        if (clusterManager?.algorithm != algorithm) {
            clusterManager?.algorithm = algorithm
        }

        if (clusterManager?.renderer != renderer) {
            clusterManager?.renderer = renderer ?: return@SideEffect
        }
    }

    if (clusterManager != null) {
        Clustering(
            items = items,
            clusterManager = clusterManager
        )
    }
}

@Preview
@Composable
fun LoadingMapPreview() {
    LoadingMap(
        loadingImage = R.drawable.ic_map_loading_large,
        mapLoading = true,
        content = {}
    )
}
