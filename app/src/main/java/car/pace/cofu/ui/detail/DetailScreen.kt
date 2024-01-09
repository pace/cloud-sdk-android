package car.pace.cofu.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
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
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.theme.Success
import car.pace.cofu.ui.theme.Warning
import car.pace.cofu.util.Constants.DETAIL_EMPTY_PRICES_CONTENT_TYPE
import car.pace.cofu.util.Constants.DETAIL_EMPTY_PRICES_KEY
import car.pace.cofu.util.Constants.DETAIL_LAST_UPDATED_CONTENT_TYPE
import car.pace.cofu.util.Constants.DETAIL_LAST_UPDATED_KEY
import car.pace.cofu.util.Constants.DETAIL_OPENING_HOURS_CONTENT_TYPE
import car.pace.cofu.util.Constants.DETAIL_OPENING_HOURS_KEY
import car.pace.cofu.util.Constants.DETAIL_PRICE_ITEM_CONTENT_TYPE
import car.pace.cofu.util.Constants.DETAIL_PRICE_LIST_TITLE_CONTENT_TYPE
import car.pace.cofu.util.Constants.DETAIL_PRICE_LIST_TITLE_KEY
import car.pace.cofu.util.Constants.DETAIL_SPACER_BOTTOM_KEY
import car.pace.cofu.util.Constants.DETAIL_SPACER_CONTENT_TYPE
import car.pace.cofu.util.Constants.DETAIL_SPACER_TOP_KEY
import car.pace.cofu.util.Constants.DETAIL_TOP_CONTENT_CONTENT_TYPE
import car.pace.cofu.util.Constants.DETAIL_TOP_CONTENT_KEY
import car.pace.cofu.util.Constants.FADE_MAP_DURATION
import car.pace.cofu.util.IntentUtils
import car.pace.cofu.util.UiState
import car.pace.cofu.util.extension.canStartFueling
import car.pace.cofu.util.extension.distanceText
import car.pace.cofu.util.extension.formatPrice
import car.pace.cofu.util.extension.lastUpdatedText
import car.pace.cofu.util.extension.twoLineAddress
import car.pace.cofu.util.openinghours.OpeningHoursStatus
import car.pace.cofu.util.openinghours.format
import car.pace.cofu.util.openinghours.openingHoursStatus
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.poikit.poi.Address
import cloud.pace.sdk.poikit.poi.Day
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.OpeningHour
import cloud.pace.sdk.poikit.poi.OpeningHours
import cloud.pace.sdk.poikit.poi.OpeningRule
import cloud.pace.sdk.poikit.poi.Price
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Date
import java.util.UUID

@Composable
fun DetailScreen(
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()

    DetailScreenContent(
        uiState = uiState,
        userLocation = userLocation,
        onRefresh = viewModel::refresh
    )
}

@Composable
fun DetailScreenContent(
    uiState: UiState<GasStation>,
    userLocation: LatLng?,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        when (uiState) {
            is UiState.Loading -> {
                LoadingCard(
                    title = stringResource(id = R.string.gas_station_loading_title),
                    description = stringResource(id = R.string.gas_station_loading_description),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            is UiState.Success -> {
                val gasStation = uiState.data
                val canStartFueling = gasStation.canStartFueling(userLocation)
                val maxItemsInRow = 3

                // To always show 3 prices in a row, we need to use a LazyVerticalGrid here.
                // Unfortunately, a LazyVerticalGrid is not compatible with a scrollable column,
                // so we also add the other content as items to the LazyVerticalGrid.
                LazyVerticalGrid(
                    columns = GridCells.Fixed(maxItemsInRow),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    item(
                        key = DETAIL_TOP_CONTENT_KEY,
                        span = { GridItemSpan(maxItemsInRow) },
                        contentType = DETAIL_TOP_CONTENT_CONTENT_TYPE
                    ) {
                        ConstraintLayout {
                            val (map, address, closed) = createRefs()
                            val openingHoursStatus = gasStation.openingHoursStatus()

                            MapRow(
                                location = gasStation.center?.toLatLn(),
                                isClosed = openingHoursStatus == OpeningHoursStatus.Closed,
                                modifier = Modifier.constrainAs(map) {
                                    top.linkTo(parent.top)
                                    width = Dimension.matchParent
                                    height = Dimension.percent(0.45f)
                                }
                            )

                            AddressRow(
                                name = gasStation.name,
                                address = gasStation.address,
                                gasStationLocation = gasStation.center?.toLatLn(),
                                userLocation = userLocation,
                                canStartFueling = canStartFueling,
                                closedOrClosesSoon = openingHoursStatus != OpeningHoursStatus.Open,
                                modifier = Modifier.constrainAs(address) {
                                    top.linkTo(anchor = map.bottom, margin = 28.dp)
                                    width = Dimension.matchParent
                                }
                            )

                            if (openingHoursStatus != OpeningHoursStatus.Open) {
                                ClosedHint(
                                    closesAt = (openingHoursStatus as? OpeningHoursStatus.ClosesSoon)?.closesAt,
                                    modifier = Modifier.constrainAs(closed) {
                                        top.linkTo(anchor = address.bottom, margin = 16.dp)
                                        width = Dimension.matchParent
                                    }
                                )
                            }
                        }
                    }

                    val showPrices = !BuildConfig.HIDE_PRICES
                    if (showPrices) {
                        priceList(
                            maxItemsInRow = maxItemsInRow,
                            prices = gasStation.prices,
                            priceFormat = gasStation.priceFormat,
                            currency = gasStation.currency,
                            updatedAt = gasStation.updatedAt
                        )
                    }

                    item(
                        key = DETAIL_OPENING_HOURS_KEY,
                        span = { GridItemSpan(maxItemsInRow) },
                        contentType = DETAIL_OPENING_HOURS_CONTENT_TYPE
                    ) {
                        OpeningHoursList(
                            openingHours = gasStation.openingHours,
                            modifier = Modifier.padding(top = 28.dp)
                        )
                    }
                }

                val buttonModifier = Modifier.padding(top = 12.dp, bottom = 30.dp)
                val context = LocalContext.current

                if (canStartFueling) {
                    PrimaryButton(
                        text = stringResource(id = R.string.common_start_fueling),
                        modifier = buttonModifier,
                        onClick = {
                            AppKit.openFuelingApp(context, gasStation.id)
                        }
                    )
                } else {
                    SecondaryButton(
                        text = stringResource(id = R.string.common_start_navigation),
                        modifier = buttonModifier,
                        onClick = {
                            IntentUtils.startNavigation(context, gasStation)
                        }
                    )
                }
            }

            is UiState.Error -> {
                ErrorCard(
                    title = stringResource(id = R.string.general_error_title),
                    description = stringResource(id = R.string.gas_station_error_description),
                    modifier = Modifier.padding(vertical = 12.dp),
                    imageVector = Icons.Outlined.LocalGasStation,
                    buttonText = stringResource(id = R.string.common_use_retry),
                    onButtonClick = onRefresh
                )
            }
        }
    }
}

@Composable
fun MapRow(
    location: LatLng?,
    isClosed: Boolean,
    modifier: Modifier = Modifier
) {
    var loading by remember { mutableStateOf(true) }

    Box(
        modifier = modifier.clip(shape = RoundedCornerShape(12.dp))
    ) {
        DetailMap(
            location = location,
            isClosed = isClosed,
            onMapLoaded = {
                loading = false
            }
        )

        AnimatedVisibility(
            visible = loading,
            enter = fadeIn(animationSpec = tween(FADE_MAP_DURATION)),
            exit = fadeOut(animationSpec = tween(FADE_MAP_DURATION))
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_map_loading_detail),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun DetailMap(
    location: LatLng?,
    isClosed: Boolean,
    onMapLoaded: () -> Unit = {}
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        if (location != null) {
            position = CameraPosition.fromLatLngZoom(location, 13f)
        }
    }
    val googleMapOptionsFactory = remember {
        GoogleMapOptions().liteMode(true)
    }
    val properties = remember {
        MapProperties(mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
    }
    val mapUiSettings = remember {
        MapUiSettings(
            compassEnabled = false,
            indoorLevelPickerEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            rotationGesturesEnabled = false,
            scrollGesturesEnabled = false,
            scrollGesturesEnabledDuringRotateOrZoom = false,
            tiltGesturesEnabled = false,
            zoomControlsEnabled = false,
            zoomGesturesEnabled = false
        )
    }

    GoogleMap(
        cameraPositionState = cameraPositionState,
        googleMapOptionsFactory = { googleMapOptionsFactory },
        properties = properties,
        uiSettings = mapUiSettings,
        onMapClick = {},
        onMapLoaded = onMapLoaded
    ) {
        location ?: return@GoogleMap

        MarkerComposable(
            keys = arrayOf(location, isClosed),
            state = MarkerState(position = location),
            anchor = Offset(0.5f, 0.5f)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isClosed) {
                    Text(
                        text = stringResource(id = R.string.closed_label),
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .dropShadow()
                            .border(width = 1.dp, color = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(size = 8.dp))
                            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(size = 8.dp))
                            .padding(4.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 15.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .dropShadow()
                        .size(15.dp)
                        .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddressRow(
    name: String?,
    address: Address?,
    gasStationLocation: LatLng?,
    userLocation: LatLng?,
    canStartFueling: Boolean,
    closedOrClosesSoon: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Title(
                text = name ?: stringResource(id = R.string.gas_station_default_name),
                textAlign = TextAlign.Start
            )
            Description(
                text = address?.twoLineAddress().orEmpty(),
                modifier = Modifier.padding(top = 12.dp),
                textAlign = TextAlign.Start
            )
            gasStationLocation?.distanceText(userLocation)?.let {
                DistanceLabel(
                    distanceText = it,
                    canStartFueling = canStartFueling,
                    isClosed = closedOrClosesSoon
                )
            }
        }
        Image(
            painter = painterResource(id = R.drawable.ic_brand_horizontal),
            contentDescription = null,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
fun DistanceLabel(
    distanceText: String,
    canStartFueling: Boolean,
    isClosed: Boolean
) {
    val color = when {
        isClosed -> MaterialTheme.colorScheme.error
        canStartFueling -> Success
        else -> Warning
    }

    Row(
        modifier = Modifier
            .padding(top = 12.dp)
            .background(color = color, shape = RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_distance_arrow),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.background
        )
        Text(
            text = distanceText,
            modifier = Modifier.padding(start = 4.dp),
            color = MaterialTheme.colorScheme.background,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun ClosedHint(
    modifier: Modifier = Modifier,
    centerHorizontal: Boolean = false,
    closesAt: String? = null
) {
    val text = if (closesAt != null) stringResource(id = R.string.gas_station_closes_soon_hint, closesAt) else stringResource(id = R.string.gas_station_closed_hint)
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (centerHorizontal) Arrangement.Center else Arrangement.Start
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 4.dp),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

fun LazyGridScope.priceList(
    maxItemsInRow: Int,
    prices: List<Price>,
    priceFormat: String?,
    currency: String?,
    updatedAt: Date?
) {
    item(
        key = DETAIL_PRICE_LIST_TITLE_KEY,
        span = { GridItemSpan(maxItemsInRow) },
        contentType = DETAIL_PRICE_LIST_TITLE_CONTENT_TYPE
    ) {
        Text(
            text = stringResource(id = R.string.gas_station_fuel_prices_title),
            modifier = Modifier.padding(top = 28.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleSmall
        )
    }

    if (prices.isEmpty()) {
        item(
            key = DETAIL_EMPTY_PRICES_KEY,
            span = { GridItemSpan(maxItemsInRow) },
            contentType = DETAIL_EMPTY_PRICES_CONTENT_TYPE
        ) {
            Text(
                text = stringResource(id = R.string.gas_station_fuel_prices_not_available),
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 18.sp
            )
        }
    } else {
        item(
            key = DETAIL_SPACER_TOP_KEY,
            span = { GridItemSpan(maxItemsInRow) },
            contentType = DETAIL_SPACER_CONTENT_TYPE
        ) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        itemsIndexed(
            items = prices,
            key = { _, price -> price.type },
            contentType = { _, _ -> DETAIL_PRICE_ITEM_CONTENT_TYPE }
        ) { index, price ->
            val firstItemInRow = index % maxItemsInRow == 0
            val lastItemInRow = (index + 1) % maxItemsInRow == 0

            PriceListItem(
                price = price,
                priceFormat = priceFormat,
                currency = currency,
                modifier = Modifier.padding(
                    start = if (firstItemInRow) 0.dp else 4.dp,
                    top = 4.dp,
                    end = if (lastItemInRow) 0.dp else 4.dp,
                    bottom = 4.dp
                )
            )
        }

        item(
            key = DETAIL_SPACER_BOTTOM_KEY,
            span = { GridItemSpan(maxItemsInRow) },
            contentType = DETAIL_SPACER_CONTENT_TYPE
        ) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (updatedAt != null) {
            item(
                key = DETAIL_LAST_UPDATED_KEY,
                span = { GridItemSpan(maxItemsInRow) },
                contentType = DETAIL_LAST_UPDATED_CONTENT_TYPE
            ) {
                Text(
                    text = updatedAt.lastUpdatedText(),
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun PriceListItem(
    price: Price,
    priceFormat: String?,
    currency: String?,
    modifier: Modifier = Modifier
) {
    val productName = price.name ?: return
    val formattedPrice = price.formatPrice(priceFormat = priceFormat, currency = currency) ?: return

    Column(
        modifier = modifier
            .height(70.dp)
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .padding(2.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = productName,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = formattedPrice,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
fun OpeningHoursList(
    openingHours: List<OpeningHours>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.gas_station_opening_hours_title),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleSmall
        )

        if (openingHours.isEmpty()) {
            Text(
                text = stringResource(id = R.string.gas_station_opening_hours_not_available),
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 18.sp
            )
        } else {
            openingHours.format().forEach {
                OpeningHoursListItem(
                    days = it.days,
                    times = it.times,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            Text(
                text = stringResource(id = R.string.gas_station_opening_hours_hint),
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun OpeningHoursListItem(
    days: String,
    times: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Description(
            text = days,
            textAlign = TextAlign.Start
        )
        Description(
            text = times,
            textAlign = TextAlign.End
        )
    }
}

@Preview
@Composable
fun DetailScreenContentPreview() {
    AppTheme {
        DetailScreenContent(
            uiState = UiState.Success(
                GasStation(UUID.randomUUID().toString(), arrayListOf()).apply {
                    name = "Gas what"
                    address = Address("c=de;l=Karlsruhe;pc=76131;s=Haid-und-Neu-Straße;hn=18")
                    latitude = 49.012440
                    longitude = 8.426530
                    prices = mutableListOf(
                        Price("diesel", "Diesel", 1.679),
                        Price("ron95e5", "Super", 1.879)
                    )
                    currency = "EUR"
                    priceFormat = "d.dds"
                }
            ),
            userLocation = LatLng(49.013513, 8.4018654),
            onRefresh = {}
        )
    }
}

@Preview
@Composable
fun MapRowPreview() {
    AppTheme {
        MapRow(
            location = LatLng(49.012440, 8.426530),
            isClosed = false,
            modifier = Modifier.height(150.dp)
        )
    }
}

@Preview
@Composable
fun AddressRowPreview() {
    AppTheme {
        AddressRow(
            name = "Gas what",
            address = Address("c=de;l=Karlsruhe;pc=76131;s=Haid-und-Neu-Straße;hn=18"),
            gasStationLocation = LatLng(49.012440, 8.426530),
            userLocation = LatLng(49.013513, 8.4018654),
            canStartFueling = true,
            closedOrClosesSoon = false
        )
    }
}

@Preview
@Composable
fun ClosedHintPreview() {
    AppTheme {
        ClosedHint()
    }
}

@Preview
@Composable
fun PriceListPreview() {
    AppTheme {
        val maxItemsInRow = 3

        LazyVerticalGrid(
            columns = GridCells.Fixed(maxItemsInRow)
        ) {
            priceList(
                maxItemsInRow = maxItemsInRow,
                prices = listOf(
                    Price("diesel", "Diesel", 1.679),
                    Price("ron95e5", "Super", 1.879),
                    Price("ron98e5", "Super Plus with very long name", 1.979),
                    Price("ron95e10", "Super E10", 1.859)
                ),
                priceFormat = "d.dds",
                currency = "EUR",
                updatedAt = Date()
            )
        }
    }
}

@Preview
@Composable
fun PriceListItemPreview() {
    AppTheme {
        PriceListItem(
            price = Price("diesel", "Diesel", 1.679),
            priceFormat = "d.dds",
            currency = "EUR"
        )
    }
}

@Preview
@Composable
fun OpeningHoursListPreview() {
    AppTheme {
        OpeningHoursList(
            openingHours = listOf(
                OpeningHours(listOf(Day.FRIDAY, Day.MONDAY, Day.THURSDAY, Day.TUESDAY, Day.WEDNESDAY), listOf(OpeningHour("8", "22")), OpeningRule.OPEN),
                OpeningHours(listOf(Day.SUNDAY), listOf(OpeningHour("0", "0")), OpeningRule.CLOSED),
                OpeningHours(listOf(Day.SATURDAY), listOf(OpeningHour("10", "20")), OpeningRule.OPEN)
            )
        )
    }
}

@Preview
@Composable
fun OpeningHoursListItemPreview() {
    AppTheme {
        OpeningHoursListItem(
            days = "Montag - Samstag",
            times = "06.00 - 23.00 Uhr"
        )
    }
}
