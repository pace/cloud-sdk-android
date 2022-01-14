package car.pace.cofu.ui.home

import android.location.Location
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import car.pace.cofu.R
import car.pace.cofu.core.events.ShowSnack
import car.pace.cofu.core.mvvm.BaseItemViewModel
import car.pace.cofu.core.mvvm.BaseViewModel
import car.pace.cofu.core.resources.ResourcesProvider
import car.pace.cofu.core.util.formattedAsMeter
import cloud.pace.sdk.appkit.app.api.AppRepositoryImpl
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.Price
import kotlinx.coroutines.launch
import kotlin.math.max

class PetrolStationItemViewModel(
    override val item: GasStation,
    _fuelTypeIdentifier: String?,
    private val resourcesProvider: ResourcesProvider,
    private val parent: BaseViewModel,
    location: Location
) : BaseItemViewModel() {
    override val layoutId = R.layout.item_petrol_station
    override val id = item.id.hashCode()

    val distanceFormatted = ObservableField<String>()
    var distance = 0.0

    internal var fuelTypeIdentifier: String? = null
        set(value) {
            field = value
            price.set(calculatePrice())
        }

    /**
     * fueling is only available when the user is less than [AppRepositoryImpl.IS_POI_IN_RANGE_DISTANCE_THRESHOLD] meters
     * away (currently 500m). Otherwise show the link to a navigation app as CTA
     */
    var isCloseEnoughForFueling = ObservableBoolean(true)

    val price = ObservableField<String>()

    val address: String?
        get() = item.address?.let { address ->
            val street = address.street ?: ""
            val houseNumber = address.houseNumber ?: ""
            val postalCode = address.postalCode ?: ""
            val city = address.city ?: ""

            "$street $houseNumber\n$postalCode $city"
        }

    private val relevantPrice: Price?
        get() = item.prices.firstOrNull { it.type.value == fuelTypeIdentifier }


    private fun calculatePrice(): String = relevantPrice
        ?.formatCurrency(item.currency)
        ?: resourcesProvider.getString(R.string.home_price_not_available)

    init {
        updateLocation(location)
        fuelTypeIdentifier = _fuelTypeIdentifier
    }

    fun onCtaClick() {
        if (isCloseEnoughForFueling.get()) {
            parent.handleEvent(HomeViewModel.FuelUpEvent(item))
        } else {
            startNavigationApp()
        }
    }

    fun startNavigationApp() {
        parent.handleEvent(HomeViewModel.StartNavigationEvent(item))
    }

    fun onPriceClick() {
        // since an empty price only displays as "n.v.", show an explanation text on click
        if (relevantPrice == null) parent.handleEvent(ShowSnack(resourcesProvider.getString(R.string.home_price_not_available_description)))
    }


    fun updateLocation(location: Location) {
        val distanceFloat = FloatArray(1)
        Location.distanceBetween(
            location.latitude,
            location.longitude,
            item.latitude!!,
            item.longitude!!,
            distanceFloat
        )
        distance = distanceFloat[0].toDouble()
        distanceFormatted.set(distance.formattedAsMeter)

        // the call is async, but it is a local check so it will be quick
        parent.viewModelScope.launch {
            isCloseEnoughForFueling.set(POIKit.isPoiInRange(item.id, location))
        }
    }

    private fun Price.formatCurrency(currency: String?): String {
        val currencySymbol = if (currency == "EUR") "€" else currency
        val priceFormatted = "%.3f".format(price)
        // set the last digit in html <sup> tags to have it superset
        val indexOfSecondToLastDigit = max(priceFormatted.length - 1, 0)
        val pricePart1 = priceFormatted.substring(0, indexOfSecondToLastDigit)
        val pricePart2 = priceFormatted.substring(indexOfSecondToLastDigit, priceFormatted.length)
        return "$pricePart1<sup><small>$pricePart2</small></sup> $currencySymbol"
    }
}