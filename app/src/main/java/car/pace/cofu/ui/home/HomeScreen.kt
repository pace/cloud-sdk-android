package car.pace.cofu.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import car.pace.cofu.R
import car.pace.cofu.ui.component.DefaultCircularProgressIndicator
import car.pace.cofu.ui.component.Description
import car.pace.cofu.ui.component.PrimaryButton
import car.pace.cofu.ui.component.SecondaryButton
import car.pace.cofu.ui.component.Title
import car.pace.cofu.ui.component.dropShadow
import car.pace.cofu.ui.home.dialog.LocationDisabledDialog
import car.pace.cofu.ui.home.dialog.LocationPermissionDialog
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.wallet.fueltype.FuelType
import car.pace.cofu.util.Constants.GAS_STATION_CONTENT_TYPE
import car.pace.cofu.util.Constants.NEAREST_GAS_STATION_TITLE_KEY
import car.pace.cofu.util.Constants.OTHER_GAS_STATIONS_TITLE_KEY
import car.pace.cofu.util.Constants.TITLE_CONTENT_TYPE
import car.pace.cofu.util.IntentUtils
import car.pace.cofu.util.SnackbarData
import car.pace.cofu.util.UiState
import car.pace.cofu.util.extension.canStartFueling
import car.pace.cofu.util.extension.distanceText
import car.pace.cofu.util.extension.formatPrice
import car.pace.cofu.util.extension.isLocationEnabled
import car.pace.cofu.util.extension.isLocationPermissionGranted
import car.pace.cofu.util.extension.listenForLocationEnabledChanges
import car.pace.cofu.util.extension.twoLineAddress
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.poikit.poi.Address
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.Price
import com.google.android.gms.maps.model.LatLng
import java.util.UUID
import timber.log.Timber

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    showSnackbar: (SnackbarData) -> Unit,
    navigateToDetail: (String) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = viewModel.showPullRefreshIndicator,
        onRefresh = viewModel::onRefresh
    )

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pullRefresh(state = pullRefreshState)
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val uiState by viewModel.uiState.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
        val fuelType by viewModel.fuelType.collectAsStateWithLifecycle()
        val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()

        var showLocationPermissionDialog by remember { mutableStateOf(false) }
        var showLocationDisabledDialog by remember { mutableStateOf(false) }

        LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
            showLocationPermissionDialog = !context.isLocationPermissionGranted
            showLocationDisabledDialog = !context.isLocationEnabled
        }

        DisposableEffect(lifecycleOwner) {
            val locationEnabledListener = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    showLocationDisabledDialog = !context.isLocationEnabled
                }
            }
            context.listenForLocationEnabledChanges(locationEnabledListener)

            onDispose {
                context.unregisterReceiver(locationEnabledListener)
            }
        }

        LaunchedEffect(Unit) {
            viewModel.showSnackbarError.collect {
                if (it) {
                    showSnackbar(SnackbarData(R.string.HOME_LOADING_FAILED_TEXT))
                }
            }
        }

        when (val state = uiState) {
            is UiState.Loading -> Loading()
            is UiState.Success -> {
                val gasStations = state.data
                if (gasStations.isNotEmpty()) {
                    GasStationList(
                        gasStations = gasStations,
                        fuelType = fuelType,
                        userLocation = userLocation,
                        onStartFueling = {
                            AppKit.openFuelingApp(context, it.id)
                        },
                        onStartNavigation = {
                            IntentUtils.startNavigation(context, it)
                        },
                        onClick = {
                            navigateToDetail(it.id)
                        }
                    )
                } else {
                    Empty()
                }
            }

            is UiState.Error -> Error()
        }

        PullRefreshIndicator(
            refreshing = viewModel.showPullRefreshIndicator,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        if (showLocationPermissionDialog) {
            LocationPermissionDialog(
                onConfirm = {
                    try {
                        val uri = Uri.fromParts("package", context.packageName, null)
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Timber.e(e, "Could not launch permission settings")
                        showLocationPermissionDialog = false
                        showSnackbar(SnackbarData(R.string.DASHBOARD_PERMISSION_SETTINGS_ERROR))
                    }
                },
                onDismiss = {
                    showLocationPermissionDialog = false
                }
            )
        } else if (showLocationDisabledDialog) {
            LocationDisabledDialog(
                onConfirm = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    } catch (e: Exception) {
                        Timber.e(e, "Could not launch location settings")
                        showLocationDisabledDialog = false
                        showSnackbar(SnackbarData(R.string.DASHBOARD_LOCATION_SETTINGS_ERROR))
                    }
                },
                onDismiss = {
                    showLocationDisabledDialog = false
                }
            )
        }
    }
}

@Composable
fun GasStationList(
    gasStations: List<GasStation>,
    fuelType: FuelType?,
    userLocation: LatLng?,
    onStartFueling: (GasStation) -> Unit,
    onStartNavigation: (GasStation) -> Unit,
    onClick: (GasStation) -> Unit
) {
    val nearestGasStation = remember(gasStations) { gasStations.firstOrNull() }
    val otherGasStations = remember(gasStations) { gasStations.drop(1) }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 25.dp, vertical = 30.dp)
    ) {
        if (nearestGasStation != null) {
            item(
                key = NEAREST_GAS_STATION_TITLE_KEY,
                contentType = TITLE_CONTENT_TYPE
            ) {
                Title(
                    text = stringResource(id = R.string.DASHBOARD_SECTIONS_NEAREST_GAS_STATION),
                    textAlign = TextAlign.Start
                )
            }

            item(
                key = nearestGasStation.id,
                contentType = GAS_STATION_CONTENT_TYPE
            ) {
                GasStationRow(
                    modifier = Modifier.padding(top = 10.dp),
                    gasStation = nearestGasStation,
                    userLocation = userLocation,
                    fuelType = fuelType,
                    onStartFueling = { onStartFueling(nearestGasStation) },
                    onStartNavigation = { onStartNavigation(nearestGasStation) },
                    onClick = { onClick(nearestGasStation) }
                )
            }
        }

        item(
            key = OTHER_GAS_STATIONS_TITLE_KEY,
            contentType = TITLE_CONTENT_TYPE
        ) {
            Title(
                text = stringResource(id = R.string.DASHBOARD_SECTIONS_OTHER_GAS_STATIONS),
                modifier = Modifier.padding(top = if (nearestGasStation != null) 40.dp else 0.dp),
                textAlign = TextAlign.Start
            )
        }

        itemsIndexed(
            items = otherGasStations,
            key = { _, gasStation -> gasStation.id },
            contentType = { _, _ -> GAS_STATION_CONTENT_TYPE }
        ) { index, gasStation ->
            GasStationRow(
                modifier = Modifier.padding(top = if (index == 0) 10.dp else 16.dp),
                gasStation = gasStation,
                userLocation = userLocation,
                fuelType = fuelType,
                onStartFueling = { onStartFueling(gasStation) },
                onStartNavigation = { onStartNavigation(gasStation) },
                onClick = { onClick(gasStation) }
            )
        }
    }
}

@Composable
fun GasStationRow(
    modifier: Modifier = Modifier,
    gasStation: GasStation,
    userLocation: LatLng?,
    fuelType: FuelType?,
    onStartFueling: () -> Unit,
    onStartNavigation: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Title(
                text = gasStation.name ?: stringResource(id = R.string.gas_station_default_name),
                textAlign = TextAlign.Start
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Description(
                        text = gasStation.address?.twoLineAddress().orEmpty(),
                        textAlign = TextAlign.Start
                    )
                    gasStation.center?.toLatLn()?.distanceText(userLocation)?.let {
                        Row(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_distance_arrow),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = it,
                                modifier = Modifier.padding(start = 9.dp),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .background(color = MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 17.dp, vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    fuelType?.let {
                        Text(
                            text = stringResource(id = it.stringRes),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    val priceText = fuelType?.let { gasStation.formatPrice(fuelType = it) }
                    Title(
                        text = priceText ?: stringResource(id = R.string.PRICE_NOT_AVAILABLE)
                    )
                }
            }

            val buttonModifier = Modifier.padding(top = 19.dp)
            if (gasStation.canStartFueling(userLocation)) {
                PrimaryButton(
                    text = stringResource(id = R.string.common_start_fueling),
                    modifier = buttonModifier,
                    onClick = onStartFueling
                )
            } else {
                SecondaryButton(
                    text = stringResource(id = R.string.common_start_navigation),
                    modifier = buttonModifier,
                    onClick = onStartNavigation
                )
            }
        }
    }
}

@Composable
fun Loading() {
    NoContent(
        isLoading = true,
        title = stringResource(id = R.string.DASHBOARD_LOADING_VIEW_TITLE),
        description = stringResource(id = R.string.DASHBOARD_LOADING_VIEW_DESCRIPTION)
    )
}

@Composable
fun Empty() {
    NoContent(
        isLoading = false,
        title = stringResource(id = R.string.DASHBOARD_EMPTY_VIEW_TITLE),
        description = stringResource(id = R.string.DASHBOARD_EMPTY_VIEW_DESCRIPTION)
    )
}

@Composable
fun Error() {
    NoContent(
        isLoading = false,
        title = stringResource(id = R.string.general_error_title),
        description = stringResource(id = R.string.HOME_LOADING_FAILED_TEXT)
    )
}

@Composable
fun NoContent(
    isLoading: Boolean,
    title: String,
    description: String
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 25.dp, vertical = 30.dp)
    ) {
        val (indicatorRef, titleRef, descriptionRef) = createRefs()
        val guideline = createGuidelineFromTop(0.15f)
        val indicatorModifier = Modifier.constrainAs(indicatorRef) {
            top.linkTo(guideline)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        if (isLoading) {
            DefaultCircularProgressIndicator(
                modifier = indicatorModifier
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_no_results),
                contentDescription = null,
                modifier = indicatorModifier
            )
        }
        Title(
            text = title,
            modifier = Modifier
                .constrainAs(titleRef) {
                    top.linkTo(anchor = indicatorRef.bottom, margin = 30.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
        Description(
            text = description,
            modifier = Modifier
                .constrainAs(descriptionRef) {
                    top.linkTo(anchor = titleRef.bottom, margin = 14.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(
            showSnackbar = {},
            navigateToDetail = {}
        )
    }
}

@Preview
@Composable
fun GasStationListPreview() {
    AppTheme {
        val gasStations = remember {
            listOf(
                GasStation(UUID.randomUUID().toString(), arrayListOf()).apply {
                    name = "Gas what"
                    address = Address("c=de;l=Karlsruhe;pc=76131;s=Haid-und-Neu-Straße;hn=18")
                    latitude = 49.012440
                    longitude = 8.426530
                    prices = mutableListOf(Price("diesel", "Diesel", 1.337))
                    currency = "EUR"
                    priceFormat = "d.dds"
                },
                GasStation(UUID.randomUUID().toString(), arrayListOf()).apply {
                    name = "Gas what 2"
                    address = Address("c=de;l=Karlsruhe;pc=76131;s=Haid-und-Neu-Straße;hn=18")
                    latitude = 49.012440
                    longitude = 8.426530
                    prices = mutableListOf(Price("super", "Super", 1.537))
                    currency = "EUR"
                    priceFormat = "d.dds"
                }
            )
        }

        GasStationList(
            gasStations = gasStations,
            fuelType = FuelType.DIESEL,
            userLocation = LatLng(49.013513, 8.4018654),
            onStartFueling = {},
            onStartNavigation = {},
            onClick = {}
        )
    }
}

@Preview
@Composable
fun GasStationRowPreview() {
    AppTheme {
        val gasStation = remember {
            GasStation(UUID.randomUUID().toString(), arrayListOf()).apply {
                name = "Gas what"
                address = Address("c=de;l=Karlsruhe;pc=76131;s=Haid-und-Neu-Straße;hn=18")
                latitude = 49.012440
                longitude = 8.426530
                prices = mutableListOf(Price("diesel", "Diesel", 1.337))
                currency = "EUR"
                priceFormat = "d.dds"
            }
        }

        GasStationRow(
            modifier = Modifier.padding(20.dp),
            gasStation = gasStation,
            userLocation = LatLng(49.013513, 8.4018654),
            fuelType = FuelType.DIESEL,
            onStartFueling = {},
            onStartNavigation = {},
            onClick = {}
        )
    }
}

@Preview
@Composable
fun LoadingPreview() {
    AppTheme {
        Loading()
    }
}

@Preview
@Composable
fun EmptyPreview() {
    AppTheme {
        Empty()
    }
}

@Preview
@Composable
fun ErrorPreview() {
    AppTheme {
        Error()
    }
}
