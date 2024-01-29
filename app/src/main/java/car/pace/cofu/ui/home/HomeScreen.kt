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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import car.pace.cofu.ui.component.LogoTopBar
import car.pace.cofu.ui.component.PrimaryButton
import car.pace.cofu.ui.component.SecondaryButton
import car.pace.cofu.ui.component.Title
import car.pace.cofu.ui.component.dropShadow
import car.pace.cofu.ui.detail.ClosedHint
import car.pace.cofu.ui.detail.DistanceLabel
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.theme.Success
import car.pace.cofu.ui.wallet.fueltype.FuelTypeGroup
import car.pace.cofu.util.Constants.GAS_STATION_CONTENT_TYPE
import car.pace.cofu.util.IntentUtils
import car.pace.cofu.util.LogAndBreadcrumb
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

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val fuelTypeGroup by viewModel.fuelTypeGroup.collectAsStateWithLifecycle()
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
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

    HomeScreenContent(
        uiState = uiState,
        fuelTypeGroup = fuelTypeGroup,
        userLocation = userLocation,
        refresh = viewModel::refresh,
        navigateToDetail = navigateToDetail
    )
}

@Composable
fun HomeScreenContent(
    uiState: UiState<List<GasStation>>,
    showCustomHeader: Boolean = BuildConfig.HOME_SHOW_CUSTOM_HEADER,
    fuelTypeGroup: FuelTypeGroup,
    userLocation: LatLng?,
    refresh: () -> Unit,
    navigateToDetail: (String) -> Unit
) {
    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        if (showCustomHeader) {
            Image(
                painter = painterResource(id = R.drawable.ic_home_header),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.25f),
                contentDescription = null,
                alignment = Alignment.BottomCenter,
                contentScale = ContentScale.Crop
            )
        } else {
            LogoTopBar()
        }

        when (uiState) {
            is UiState.Loading -> LoadingContent()
            is UiState.Success -> {
                val gasStations = uiState.data
                if (gasStations.isNotEmpty()) {
                    val context = LocalContext.current

                    GasStationList(
                        gasStations = gasStations,
                        fuelTypeGroup = fuelTypeGroup,
                        userLocation = userLocation,
                        onStartFueling = {
                            LogAndBreadcrumb.i(LogAndBreadcrumb.HOME, "Start fueling")
                            AppKit.openFuelingApp(context, it.id)
                        },
                        onStartNavigation = {
                            LogAndBreadcrumb.i(LogAndBreadcrumb.HOME, "Start navigation to gas station")
                            IntentUtils.startNavigation(context, it)
                        },
                        onClick = {
                            navigateToDetail(it.id)
                        }
                    )
                } else {
                    EmptyContent()
                }
            }

            is UiState.Error -> {
                when (uiState.throwable) {
                    is HomeViewModel.LocationPermissionDenied -> LocationPermissionDenied()
                    is HomeViewModel.LocationDisabled -> LocationDisabled()
                    else -> LoadingError(refresh)
                }
            }
        }
    }
}

@Composable
fun GasStationList(
    gasStations: List<GasStation>,
    fuelTypeGroup: FuelTypeGroup,
    userLocation: LatLng?,
    onStartFueling: (GasStation) -> Unit,
    onStartNavigation: (GasStation) -> Unit,
    onClick: (GasStation) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(
            items = gasStations,
            key = GasStation::id,
            contentType = { GAS_STATION_CONTENT_TYPE }
        ) {
            GasStationRow(
                gasStation = it,
                userLocation = userLocation,
                fuelTypeGroup = fuelTypeGroup,
                onStartFueling = { onStartFueling(it) },
                onStartNavigation = { onStartNavigation(it) },
                onClick = { onClick(it) }
            )
        }
    }
}

@Composable
fun GasStationRow(
    modifier: Modifier = Modifier,
    gasStation: GasStation,
    userLocation: LatLng?,
    fuelTypeGroup: FuelTypeGroup,
    onStartFueling: () -> Unit,
    onStartNavigation: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow()
            .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(8.dp))
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
                    .background(color = Success, shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp))
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
                        DistanceLabel(
                            distanceText = it,
                            canStartFueling = canStartFueling,
                            isClosed = openingHoursStatus != OpeningHoursStatus.Open
                        )
                    }
                }
                if (showPrices) {
                    Column(
                        modifier = Modifier
                            .padding(start = 5.dp, top = 5.dp)
                            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 17.dp, vertical = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = stringResource(id = fuelTypeGroup.stringRes),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium
                        )

                        Title(
                            text = gasStation.formatPrice(fuelTypeGroup = fuelTypeGroup) ?: stringResource(id = R.string.PRICE_NOT_AVAILABLE)
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
                    modifier = Modifier.padding(top = 16.dp),
                    centerHorizontal = true,
                    closesAt = (openingHoursStatus as? OpeningHoursStatus.ClosesSoon)?.closesAt
                )
            }
        }
    }
}

@Composable
fun LoadingContent() {
    LoadingCard(
        title = stringResource(id = R.string.DASHBOARD_LOADING_VIEW_TITLE),
        description = stringResource(id = R.string.DASHBOARD_LOADING_VIEW_DESCRIPTION),
        modifier = Modifier.padding(20.dp)
    )
}

@Composable
fun EmptyContent() {
    ErrorCard(
        title = stringResource(id = R.string.DASHBOARD_EMPTY_VIEW_TITLE),
        description = stringResource(id = R.string.DASHBOARD_EMPTY_VIEW_DESCRIPTION),
        modifier = Modifier.padding(20.dp),
        imageVector = Icons.Outlined.LocalGasStation
    )
}

@Composable
fun LoadingError(onRetryButtonClick: () -> Unit) {
    ErrorCard(
        title = stringResource(id = R.string.general_error_title),
        description = stringResource(id = R.string.HOME_LOADING_FAILED_TEXT),
        modifier = Modifier.padding(20.dp),
        buttonText = stringResource(id = R.string.common_use_retry),
        onButtonClick = onRetryButtonClick
    )
}

@Composable
fun LocationDisabled() {
    val context = LocalContext.current
    ErrorCard(
        title = stringResource(id = R.string.LOCATION_DIALOG_DISABLED_TITLE),
        description = stringResource(id = R.string.LOCATION_DIALOG_DISABLED_TEXT),
        modifier = Modifier.padding(20.dp),
        buttonText = stringResource(id = R.string.ALERT_LOCATION_PERMISSION_ACTIONS_OPEN_SETTINGS)
    ) {
        openLocationSettings(context)
    }
}

@Composable
fun LocationPermissionDenied() {
    val context = LocalContext.current
    ErrorCard(
        title = stringResource(id = R.string.LOCATION_DIALOG_PERMISSION_DENIED_TITLE),
        description = stringResource(id = R.string.LOCATION_DIALOG_PERMISSION_DENIED_TEXT),
        modifier = Modifier.padding(20.dp),
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
        LogAndBreadcrumb.e(e, LogAndBreadcrumb.HOME, "Could not launch permission settings")
    }
}

private fun openLocationSettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    } catch (e: Exception) {
        LogAndBreadcrumb.e(e, LogAndBreadcrumb.HOME, "Could not launch location settings")
    }
}

@Preview
@Composable
fun HomeScreenCustomHeaderPreview() {
    HomeScreenPreview(showCustomHeader = true)
}

@Preview
@Composable
fun HomeScreenDefaultPreview() {
    HomeScreenPreview(showCustomHeader = false)
}

@Composable
private fun HomeScreenPreview(
    showCustomHeader: Boolean
) {
    AppTheme {
        HomeScreenContent(
            uiState = UiState.Success(
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
                        prices = mutableListOf(Price("ron95e5", "Petrol", 1.537))
                        currency = "EUR"
                        priceFormat = "d.dds"
                    }
                )
            ),
            showCustomHeader = showCustomHeader,
            fuelTypeGroup = FuelTypeGroup.DIESEL,
            userLocation = LatLng(49.013513, 8.4018654),
            refresh = {},
            navigateToDetail = {}
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
                prices = mutableListOf(Price("ron95e5", "Petrol", 1.537))
                currency = "EUR"
                priceFormat = "d.dds"
            }
        }

        GasStationRow(
            modifier = Modifier.padding(20.dp),
            gasStation = gasStation,
            userLocation = LatLng(49.013513, 8.4018654),
            fuelTypeGroup = FuelTypeGroup.PETROL,
            onStartFueling = {},
            onStartNavigation = {},
            onClick = {}
        )
    }
}

@Preview
@Composable
fun LoadingContentPreview() {
    AppTheme {
        LoadingContent()
    }
}

@Preview
@Composable
fun EmptyContentPreview() {
    AppTheme {
        EmptyContent()
    }
}

@Preview
@Composable
fun LoadingErrorPreview() {
    AppTheme {
        LoadingError {}
    }
}

@Preview
@Composable
fun LocationDisabledPreview() {
    AppTheme {
        LocationDisabled()
    }
}

@Preview
@Composable
fun LocationPermissionDeniedPreview() {
    AppTheme {
        LocationPermissionDenied()
    }
}
