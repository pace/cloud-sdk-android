package car.pace.cofu.ui.list

import android.app.Activity
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
import car.pace.cofu.ui.location.rememberLocationState
import car.pace.cofu.ui.onboarding.twofactor.biometric.findActivity
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.theme.Success
import car.pace.cofu.ui.wallet.fueltype.FuelTypeGroup
import car.pace.cofu.util.Constants.GAS_STATION_CONTENT_TYPE
import car.pace.cofu.util.IntentUtils
import car.pace.cofu.util.LogAndBreadcrumb
import car.pace.cofu.util.UiState
import car.pace.cofu.util.extension.LocationDisabledException
import car.pace.cofu.util.extension.LocationPermissionDeniedException
import car.pace.cofu.util.extension.canShowLocationPermissionDialog
import car.pace.cofu.util.extension.distanceText
import car.pace.cofu.util.extension.formatPrice
import car.pace.cofu.util.extension.oneLineAddress
import car.pace.cofu.util.extension.twoLineAddress
import car.pace.cofu.util.openinghours.OpeningHoursStatus
import car.pace.cofu.util.openinghours.openingHoursStatus
import cloud.pace.sdk.poikit.poi.Address
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.Price
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import java.util.UUID
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ListScreen(
    viewModel: ListViewModel = hiltViewModel(),
    navigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val fuelTypeGroup by viewModel.fuelTypeGroup.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val locationState = rememberLocationState(
        onLocationEnabledChanged = viewModel::onLocationEnabledChanged,
        onLocationPermissionChanged = viewModel::onLocationPermissionChanged
    )

    ListScreenContent(
        uiState = uiState,
        fuelTypeGroup = fuelTypeGroup,
        refresh = viewModel::refresh,
        navigateToDetail = navigateToDetail,
        onRequestPermission = locationState::launchMultiplePermissionRequest,
        onRequestLocationServices = locationState::launchLocationServicesRequest,
        onStartFueling = {
            viewModel.startFueling(context, it)
        },
        onStartNavigation = {
            viewModel.startNavigation(context, it)
        }
    )
}

@Composable
fun ListScreenContent(
    uiState: UiState<List<ListViewModel.ListStation>>,
    showCustomHeader: Boolean = BuildConfig.LIST_SHOW_CUSTOM_HEADER,
    fuelTypeGroup: FuelTypeGroup,
    refresh: () -> Unit,
    navigateToDetail: (String) -> Unit,
    onRequestPermission: () -> Unit,
    onRequestLocationServices: suspend () -> Boolean,
    onStartFueling: (GasStation) -> Unit,
    onStartNavigation: (GasStation) -> Unit
) {
    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        if (showCustomHeader) {
            Image(
                painter = painterResource(id = R.drawable.ic_list_header),
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
                    GasStationList(
                        gasStations = gasStations,
                        fuelTypeGroup = fuelTypeGroup,
                        onStartFueling = onStartFueling,
                        onStartNavigation = {
                            onStartNavigation(it)
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
                    is LocationDisabledException -> LocationDisabled(onRequestLocationServices)
                    is LocationPermissionDeniedException -> LocationPermissionDenied(onRequestPermission)
                    else -> LoadingError(refresh)
                }
            }
        }
    }
}

@Composable
fun GasStationList(
    gasStations: List<ListViewModel.ListStation>,
    fuelTypeGroup: FuelTypeGroup,
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
            key = { it.gasStation.id },
            contentType = { GAS_STATION_CONTENT_TYPE }
        ) {
            GasStationRow(
                listStation = it,
                fuelTypeGroup = fuelTypeGroup,
                onStartFueling = { onStartFueling(it.gasStation) },
                onStartNavigation = { onStartNavigation(it.gasStation) },
                onClick = { onClick(it.gasStation) }
            )
        }
    }
}

@Composable
fun GasStationRow(
    modifier: Modifier = Modifier,
    listStation: ListViewModel.ListStation,
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
        val context = LocalContext.current
        val gasStation = listStation.gasStation
        val canStartFueling = listStation.canStartFueling
        val openingHoursStatus = gasStation.openingHoursStatus()
        val address = gasStation.address
        val showPrices = !BuildConfig.HIDE_PRICES
        val distanceText = listStation.distanceText(context)

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
                    distanceText?.let {
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
fun LocationDisabled(
    onRequestLocationServices: suspend () -> Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    ErrorCard(
        title = stringResource(id = R.string.LOCATION_DIALOG_DISABLED_TITLE),
        description = stringResource(id = R.string.LOCATION_DIALOG_DISABLED_TEXT),
        modifier = Modifier.padding(20.dp),
        buttonText = stringResource(id = R.string.ALERT_LOCATION_PERMISSION_ACTIONS_OPEN_SETTINGS)
    ) {
        coroutineScope.launch {
            val successful = onRequestLocationServices()
            if (!successful) {
                IntentUtils.openLocationSettings(context).onFailure {
                    LogAndBreadcrumb.e(it, LogAndBreadcrumb.LIST, "Could not launch location settings")
                }
            }
        }
    }
}

@Composable
fun LocationPermissionDenied(
    onRequestPermission: () -> Unit
) {
    val context = LocalContext.current

    ErrorCard(
        title = stringResource(id = R.string.LOCATION_DIALOG_PERMISSION_DENIED_TITLE),
        description = stringResource(id = R.string.LOCATION_DIALOG_PERMISSION_DENIED_TEXT),
        modifier = Modifier.padding(20.dp),
        buttonText = stringResource(id = R.string.ALERT_LOCATION_PERMISSION_ACTIONS_OPEN_SETTINGS)
    ) {
        val activity = context.findActivity<Activity>()
        if (activity.canShowLocationPermissionDialog()) {
            // System permission dialog can be shown
            onRequestPermission()
        } else {
            // User denied permission - open application settings
            IntentUtils.openAppSettings(context).onFailure {
                LogAndBreadcrumb.e(it, LogAndBreadcrumb.LIST, "Could not launch application settings")
            }
        }
    }
}

@Preview
@Composable
fun ListScreenCustomHeaderPreview() {
    ListScreenContentPreview(showCustomHeader = true)
}

@Preview
@Composable
fun ListScreenDefaultPreview() {
    ListScreenContentPreview(showCustomHeader = false)
}

@Composable
private fun ListScreenContentPreview(
    showCustomHeader: Boolean
) {
    AppTheme {
        ListScreenContent(
            uiState = UiState.Success(
                listOf(
                    ListViewModel.ListStation(
                        gasStation = GasStation(UUID.randomUUID().toString(), arrayListOf()).apply {
                            name = "Gas what"
                            address = Address("c=de;l=Karlsruhe;pc=76131;s=Haid-und-Neu-Straße;hn=18")
                            prices = mutableListOf(Price("diesel", "Diesel", 1.337))
                            currency = "EUR"
                            priceFormat = "d.dds"
                        },
                        canStartFueling = true,
                        distance = 10.0
                    ),
                    ListViewModel.ListStation(
                        gasStation = GasStation(UUID.randomUUID().toString(), arrayListOf()).apply {
                            name = "Tanke Emma"
                            address = Address("c=de;l=Karlsruhe;pc=76131;s=Haid-und-Neu-Straße;hn=18")
                            prices = mutableListOf(Price("ron95e5", "Petrol", 1.537))
                            currency = "EUR"
                            priceFormat = "d.dds"
                        },
                        canStartFueling = false,
                        distance = null
                    )
                )
            ),
            showCustomHeader = showCustomHeader,
            fuelTypeGroup = FuelTypeGroup.DIESEL,
            refresh = {},
            navigateToDetail = {},
            onRequestPermission = {},
            onRequestLocationServices = { true },
            onStartFueling = {},
            onStartNavigation = {}
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
            listStation = ListViewModel.ListStation(gasStation, true, 10.0),
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
        LocationDisabled { true }
    }
}

@Preview
@Composable
fun LocationPermissionDeniedPreview() {
    AppTheme {
        LocationPermissionDenied {}
    }
}
