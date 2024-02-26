package car.pace.cofu.ui.map

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import car.pace.cofu.BuildConfig
import car.pace.cofu.R
import car.pace.cofu.data.PermissionRepository.Companion.locationPermissions
import car.pace.cofu.ui.component.ClusterMarker
import car.pace.cofu.ui.component.DefaultDialog
import car.pace.cofu.ui.component.ExpandableFloatingActionButton
import car.pace.cofu.ui.component.GasStationMarker
import car.pace.cofu.ui.component.LoadingMap
import car.pace.cofu.ui.component.MarkerAnchor
import car.pace.cofu.ui.component.NonHierarchicalClustering
import car.pace.cofu.ui.component.SearchFloatingActionButton
import car.pace.cofu.ui.location.rememberLocationState
import car.pace.cofu.ui.map.MapViewModel.Companion.fallbackLocation
import car.pace.cofu.ui.onboarding.twofactor.biometric.findActivity
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.wallet.fueltype.FuelTypeGroup
import car.pace.cofu.util.Constants.COUNTRY_ZOOM_LEVEL
import car.pace.cofu.util.Constants.STREET_ZOOM_LEVEL
import car.pace.cofu.util.IntentUtils
import car.pace.cofu.util.LogAndBreadcrumb
import car.pace.cofu.util.UiState
import car.pace.cofu.util.data
import car.pace.cofu.util.extension.LocationDisabledException
import car.pace.cofu.util.extension.LocationPermissionDeniedException
import car.pace.cofu.util.extension.canShowLocationPermissionDialog
import car.pace.cofu.util.extension.errorTextRes
import car.pace.cofu.util.extension.formatPrice
import car.pace.cofu.util.openinghours.isClosed
import car.pace.cofu.util.throwable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    navigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val fuelTypeGroup by viewModel.fuelTypeGroup.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    val permissionState = rememberMultiplePermissionsState(permissions = locationPermissions) {
        it.forEach { (permission, isGranted) ->
            LogAndBreadcrumb.i(LogAndBreadcrumb.MAP, "$permission ${if (isGranted) "is granted" else "is not granted"}")
        }
    }
    val locationState = rememberLocationState(
        permissionState = permissionState,
        onLocationEnabledChanged = viewModel::onLocationEnabledChanged,
        onLocationPermissionChanged = viewModel::onLocationPermissionChanged
    )

    MapScreenContent(
        uiState = uiState,
        userLocation = userLocation,
        fuelTypeGroup = fuelTypeGroup,
        searchQuery = searchQuery,
        searchResults = searchResults,
        onRequestPermission = locationState::launchMultiplePermissionRequest,
        onRequestLocationServices = locationState::launchLocationServicesRequest,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSearchResultClick = viewModel::onSearchResultClick,
        onMapMovementStop = viewModel::onMapMovementStop,
        onMarkerClick = navigateToDetail
    )
}

@Composable
fun MapScreenContent(
    uiState: UiState<List<MarkerItem>>,
    userLocation: UiState<LatLng>,
    fuelTypeGroup: FuelTypeGroup,
    searchQuery: TextFieldValue,
    searchResults: List<AutocompletePrediction>,
    onRequestPermission: () -> Unit,
    onRequestLocationServices: suspend () -> Boolean,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    onSearchResultClick: suspend (AutocompletePrediction) -> Place?,
    onMapMovementStop: (VisibleRegion?, Float) -> Unit,
    onMarkerClick: (String) -> Unit
) {
    Box {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        var mapLoading by rememberSaveable { mutableStateOf(true) }
        var searchExpanded by rememberSaveable { mutableStateOf(false) }
        var followLocation by rememberSaveable { mutableStateOf(true) }
        var settingsDialog by remember { mutableStateOf<Throwable?>(null) }
        var showErrorText by remember(uiState.throwable, userLocation.throwable) {
            mutableStateOf(uiState is UiState.Error || userLocation is UiState.Error)
        }
        val cameraPositionState = rememberCameraPositionState {
            val location = userLocation.data
            val zoomLevel = if (location != null) STREET_ZOOM_LEVEL else COUNTRY_ZOOM_LEVEL
            position = CameraPosition.fromLatLngZoom(location ?: fallbackLocation, zoomLevel)
        }

        LaunchedEffect(followLocation, userLocation) {
            val location = userLocation.data
            if (followLocation && location != null) {
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(location, STREET_ZOOM_LEVEL))
            }
        }

        val currentOnMapMovementStop by rememberUpdatedState(onMapMovementStop)
        LaunchedEffect(mapLoading, cameraPositionState.isMoving) {
            if (cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE ||
                cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.API_ANIMATION
            ) {
                followLocation = false

                if (uiState !is UiState.Error) {
                    // Only location errors are dismissible, gas station loading errors should always be displayed
                    showErrorText = false
                }
            } else {
                currentOnMapMovementStop(cameraPositionState.projection?.visibleRegion, cameraPositionState.position.zoom)
            }
        }

        LoadingMap(
            loadingImage = R.drawable.ic_map_loading_large,
            mapLoading = mapLoading,
            cameraPositionState = cameraPositionState,
            onMapClick = { searchExpanded = false },
            onMapLoaded = { mapLoading = false },
            contentPadding = PaddingValues(start = 15.dp, top = 120.dp) // Adjust position of compass
        ) {
            if (uiState is UiState.Success) {
                NonHierarchicalClustering(
                    items = uiState.data,
                    onClusterItemClick = {
                        onMarkerClick(it.markerDetails.id)
                        true
                    },
                    clusterContent = {
                        ClusterMarker(
                            count = it.size,
                            modifier = Modifier.padding(15.dp)
                        )
                    },
                    clusterItemContent = {
                        key(it.markerDetails.id) {
                            if (it.markerDetails is FullMarkerDetails) {
                                val name = it.markerDetails.gasStation.name
                                val formattedPrice = if (!BuildConfig.HIDE_PRICES) it.markerDetails.gasStation.formatPrice(fuelTypeGroup = fuelTypeGroup) else null
                                val isClosed = it.markerDetails.gasStation.isClosed()

                                GasStationMarker(
                                    name = name,
                                    formattedPrice = formattedPrice,
                                    isClosed = isClosed
                                )
                            } else {
                                MarkerAnchor(modifier = Modifier.padding(10.dp))
                            }
                        }
                    }
                )
            }
        }

        SearchFloatingActionButton(
            query = searchQuery,
            results = searchResults,
            expanded = searchExpanded,
            modifier = Modifier
                .statusBarsPadding()
                .padding(20.dp),
            onQueryChange = onSearchQueryChange,
            onResultClick = {
                coroutineScope.launch {
                    onSearchResultClick(it)?.latLng?.let {
                        followLocation = false
                        searchExpanded = false
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, STREET_ZOOM_LEVEL))
                    }
                }
            },
            onClick = { searchExpanded = !searchExpanded }
        )

        val locationAvailable = userLocation is UiState.Success
        val error = if (showErrorText) uiState.throwable ?: userLocation.throwable else null

        ExpandableFloatingActionButton(
            imageVector = if (followLocation) Icons.Filled.Navigation else Icons.Outlined.Navigation,
            text = error?.errorTextRes()?.let { stringResource(id = it) },
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.BottomEnd),
            tint = if (locationAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
            onClick = {
                if (locationAvailable) {
                    followLocation = true
                } else {
                    coroutineScope.launch {
                        when (val locationError = userLocation.throwable) {
                            is LocationDisabledException -> {
                                val successful = onRequestLocationServices()
                                if (!successful) {
                                    settingsDialog = locationError
                                }
                            }

                            is LocationPermissionDeniedException -> {
                                val activity = context.findActivity<Activity>()
                                if (activity.canShowLocationPermissionDialog()) {
                                    // System permission dialog can be shown
                                    onRequestPermission()
                                } else {
                                    // User denied permission - show custom dialog
                                    settingsDialog = locationError
                                }
                            }
                        }
                    }
                }
            }
        )

        when (settingsDialog) {
            is LocationDisabledException -> LocationDisabled { settingsDialog = null }
            is LocationPermissionDeniedException -> LocationPermissionDenied { settingsDialog = null }
        }
    }
}

@Composable
fun LocationDisabled(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    InsufficientLocationDialog(
        title = stringResource(id = R.string.LOCATION_DIALOG_DISABLED_TITLE),
        text = stringResource(id = R.string.LOCATION_DIALOG_DISABLED_TEXT),
        confirmButtonText = stringResource(id = R.string.ALERT_LOCATION_PERMISSION_ACTIONS_OPEN_SETTINGS),
        onConfirm = {
            onDismiss()
            IntentUtils.openLocationSettings(context).onFailure {
                LogAndBreadcrumb.e(it, LogAndBreadcrumb.MAP, "Could not launch location settings")
            }
        },
        onDismiss = onDismiss
    )
}

@Composable
fun LocationPermissionDenied(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    InsufficientLocationDialog(
        title = stringResource(id = R.string.LOCATION_DIALOG_PERMISSION_DENIED_TITLE),
        text = stringResource(id = R.string.LOCATION_DIALOG_PERMISSION_DENIED_TEXT),
        confirmButtonText = stringResource(id = R.string.ALERT_LOCATION_PERMISSION_ACTIONS_OPEN_SETTINGS),
        onConfirm = {
            onDismiss()
            IntentUtils.openAppSettings(context).onFailure {
                LogAndBreadcrumb.e(it, LogAndBreadcrumb.MAP, "Could not launch application settings")
            }
        },
        onDismiss = onDismiss
    )
}

@Composable
fun InsufficientLocationDialog(
    title: String,
    text: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DefaultDialog(
        title = title,
        text = text,
        confirmButtonText = confirmButtonText,
        dismissButtonText = stringResource(id = R.string.common_use_cancel),
        imageVector = Icons.Outlined.LocationOff,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Preview
@Composable
fun MapScreenSuccessContentPreview() {
    MapScreenContentPreview(userLocation = UiState.Success(LatLng(49.013513, 8.4018654)))
}

@Preview
@Composable
fun MapScreenErrorContentPreview() {
    MapScreenContentPreview(userLocation = UiState.Error(LocationDisabledException()))
}

@Composable
private fun MapScreenContentPreview(
    userLocation: UiState<LatLng>
) {
    AppTheme {
        MapScreenContent(
            uiState = UiState.Success(emptyList()),
            userLocation = userLocation,
            fuelTypeGroup = FuelTypeGroup.PETROL,
            searchQuery = TextFieldValue(),
            searchResults = emptyList(),
            onRequestPermission = {},
            onRequestLocationServices = { true },
            onSearchQueryChange = {},
            onSearchResultClick = { null },
            onMapMovementStop = { _, _ -> },
            onMarkerClick = {}
        )
    }
}

@Preview
@Composable
fun LocationDisabledDialogPreview() {
    AppTheme {
        LocationDisabled {}
    }
}

@Preview
@Composable
fun LocationPermissionDialogPreview() {
    AppTheme {
        LocationPermissionDenied {}
    }
}
