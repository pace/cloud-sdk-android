package car.pace.cofu.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import car.pace.cofu.BuildConfig
import car.pace.cofu.R
import car.pace.cofu.ui.component.Description
import car.pace.cofu.ui.component.ErrorCard
import car.pace.cofu.ui.component.LoadingCard
import car.pace.cofu.ui.component.PrimaryButton
import car.pace.cofu.ui.component.SecondaryButton
import car.pace.cofu.ui.component.Title
import car.pace.cofu.ui.component.dropShadow
import car.pace.cofu.ui.detail.ClosedHint
import car.pace.cofu.ui.detail.DistanceLabel
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.theme.Success
import car.pace.cofu.ui.wallet.fueltype.FuelType
import car.pace.cofu.util.Constants.GAS_STATION_CONTENT_TYPE
import car.pace.cofu.util.IntentUtils
import car.pace.cofu.util.UiState
import car.pace.cofu.util.extension.canStartFueling
import car.pace.cofu.util.extension.distanceText
import car.pace.cofu.util.extension.formatPrice
import car.pace.cofu.util.extension.isLocationEnabled
import car.pace.cofu.util.extension.isLocationPermissionGranted
import car.pace.cofu.util.extension.listenForLocationEnabledChanges
import car.pace.cofu.util.extension.oneLineAddress
import car.pace.cofu.util.extension.twoLineAddress
import car.pace.cofu.util.openinghours.OpeningHoursStatus
import car.pace.cofu.util.openinghours.openingHoursStatus
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.poikit.poi.Address
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.Price
import com.google.android.gms.maps.model.LatLng
import java.util.UUID
import timber.log.Timber

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val fuelType by viewModel.fuelType.collectAsStateWithLifecycle()
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
            viewModel.onLocationPermissionChanged(context.isLocationPermissionGranted)
            viewModel.onLocationEnabledChanged(context.isLocationEnabled)
        }

        DisposableEffect(lifecycleOwner) {
            val locationEnabledListener = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    viewModel.onLocationEnabledChanged(context.isLocationEnabled)
                }
            }
            context.listenForLocationEnabledChanges(locationEnabledListener)

            onDispose {
                context.unregisterReceiver(locationEnabledListener)
            }
        }

        Column {
            if (BuildConfig.HOME_SHOW_CUSTOM_HEADER) {
                Image(
                    painter = painterResource(id = R.drawable.ic_home_header),
                    modifier = Modifier
                        .fillMaxHeight(0.25f)
                        .fillMaxSize(),
                    contentDescription = null,
                    alignment = Alignment.BottomCenter,
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.padding(20.dp)) {
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
                    is UiState.Error -> {
                        when (state.throwable) {
                            is HomeViewModel.LocationPermissionDenied -> LocationPermissionDenied(context)
                            is HomeViewModel.LocationDisabled -> LocationDisabled(context)
                            else -> LoadingError(viewModel::refresh)
                        }
                    }
                }
            }
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
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        itemsIndexed(
            items = gasStations,
            key = { _, gasStation -> gasStation.id },
            contentType = { _, _ -> GAS_STATION_CONTENT_TYPE }
        ) { index, gasStation ->
            GasStationRow(
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow()
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        val canStartFueling = gasStation.canStartFueling(userLocation)
        val openingHoursStatus = gasStation.openingHoursStatus()
        val address = gasStation.address
        val showPrices = !BuildConfig.HIDE_PRICES

        if (canStartFueling) {
            Text(
                text = stringResource(id = R.string.common_pay_here_now),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.surface,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Success, shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp))
                    .padding(8.dp)
            )
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Title(
                        text = gasStation.name ?: stringResource(id = R.string.gas_station_default_name),
                        textAlign = TextAlign.Start
                    )
                    Description(
                        text = if (showPrices) address?.twoLineAddress().orEmpty() else address?.oneLineAddress().orEmpty(),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    gasStation.center?.toLatLn()?.distanceText(userLocation)?.let {
                        DistanceLabel(distanceText = it, canStartFueling = canStartFueling, isClosed = openingHoursStatus != OpeningHoursStatus.Open)
                    }
                }
                if (showPrices) {
                    Column(
                        modifier = Modifier
                            .padding(start = 5.dp, top = 5.dp)
                            .background(color = MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 17.dp, vertical = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
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
            }

            val buttonModifier = Modifier.padding(top = 19.dp)
            if (canStartFueling) {
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

            if (openingHoursStatus != OpeningHoursStatus.Open) {
                ClosedHint(
                    Modifier.padding(top = 16.dp),
                    centerHorizontal = true,
                    closesAt = (openingHoursStatus as? OpeningHoursStatus.ClosesSoon)?.closesAt
                )
            }
        }
    }
}

@Composable
fun Loading() {
    LoadingCard(
        title = stringResource(id = R.string.DASHBOARD_LOADING_VIEW_TITLE),
        description = stringResource(id = R.string.DASHBOARD_LOADING_VIEW_DESCRIPTION)
    )
}

@Composable
fun Empty() {
    ErrorCard(
        title = stringResource(id = R.string.DASHBOARD_EMPTY_VIEW_TITLE),
        description = stringResource(id = R.string.DASHBOARD_EMPTY_VIEW_DESCRIPTION),
        imageVector = Icons.Outlined.LocalGasStation
    )
}

@Composable
fun LoadingError(onRetryButtonClick: () -> Unit) {
    ErrorCard(
        title = stringResource(id = R.string.general_error_title),
        description = stringResource(id = R.string.HOME_LOADING_FAILED_TEXT),
        buttonText = stringResource(id = R.string.common_use_retry),
        onButtonClick = onRetryButtonClick
    )
}

@Composable
fun LocationDisabled(context: Context) {
    ErrorCard(
        title = stringResource(id = R.string.LOCATION_DIALOG_DISABLED_TITLE),
        description = stringResource(id = R.string.LOCATION_DIALOG_DISABLED_TEXT),
        buttonText = stringResource(id = R.string.ALERT_LOCATION_PERMISSION_ACTIONS_OPEN_SETTINGS)
    ) {
        openLocationSettings(context)
    }
}

@Composable
fun LocationPermissionDenied(context: Context) {
    ErrorCard(
        title = stringResource(id = R.string.LOCATION_DIALOG_PERMISSION_DENIED_TITLE),
        description = stringResource(id = R.string.LOCATION_DIALOG_PERMISSION_DENIED_TEXT),
        buttonText = stringResource(id = R.string.ALERT_LOCATION_PERMISSION_ACTIONS_OPEN_SETTINGS)
    ) {
        openLocationPermissionSettings(context)
    }
}

private fun openLocationPermissionSettings(context: Context) {
    try {
        val uri = Uri.fromParts("package", context.packageName, null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e, "Could not launch permission settings")
    }
}

private fun openLocationSettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    } catch (e: Exception) {
        Timber.e(e, "Could not launch location settings")
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(
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
                    longitude = 8.4018654
                    prices = mutableListOf(Price("diesel", "Diesel", 1.337))
                    currency = "EUR"
                    priceFormat = "d.dds"
                },
                GasStation(UUID.randomUUID().toString(), arrayListOf()).apply {
                    name = "Tanke Emma"
                    address = Address("c=de;l=Karlsruhe;pc=76131;s=Haid-und-Neu-Straße;hn=18")
                    latitude = 49.013513
                    longitude = 8.426530
                    prices = mutableListOf(Price("diesel", "Diesel", 1.337))
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
