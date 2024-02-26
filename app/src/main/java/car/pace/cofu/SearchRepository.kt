package car.pace.cofu

import car.pace.cofu.util.LogAndBreadcrumb
import car.pace.cofu.util.extension.toRectangularBounds
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class SearchRepository @Inject constructor(
    private val placesClient: PlacesClient
) {

    /**
     * Place Autocomplete uses session tokens to group the query and selection phases of a user autocomplete search into a discrete session for billing purposes.
     * The session begins when the user starts typing a query, and concludes when they select a place and a call to Place Details is made.
     * Each session can have multiple autocomplete queries, followed by one place selection.
     */
    private var sessionToken: AutocompleteSessionToken? = null

    suspend fun findPredictions(query: String, location: LatLng?) = runCatching {
        val cancellationTokenSource = CancellationTokenSource()
        val request = FindAutocompletePredictionsRequest.builder()
            .setTypesFilter(listOf(PlaceTypes.GEOCODE))
            .setSessionToken(getSessionToken())
            .setQuery(query)
            .setCancellationToken(cancellationTokenSource.token)
            .also {
                if (location != null) {
                    it.setLocationBias(location.toRectangularBounds(OFFSET_RADIUS_METERS))
                }
            }
            .build()

        placesClient.findAutocompletePredictions(request).await(cancellationTokenSource).autocompletePredictions
    }.onFailure {
        LogAndBreadcrumb.e(it, LogAndBreadcrumb.SEARCH, "Could not find autocomplete predictions for '$query'")
    }

    suspend fun findPlace(placeId: String) = runCatching {
        val cancellationTokenSource = CancellationTokenSource()
        val placeFields = listOf(Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.builder(placeId, placeFields)
            .setSessionToken(getSessionToken())
            .setCancellationToken(cancellationTokenSource.token)
            .build()

        placesClient.fetchPlace(request).await(cancellationTokenSource).place
    }.onSuccess {
        sessionToken = null
    }.onFailure {
        LogAndBreadcrumb.e(it, LogAndBreadcrumb.SEARCH, "Could not find place with the ID: $placeId")
    }

    private fun getSessionToken(): AutocompleteSessionToken {
        return sessionToken ?: AutocompleteSessionToken.newInstance().also {
            sessionToken = it
        }
    }

    companion object {
        private const val OFFSET_RADIUS_METERS = 100_000.0
    }
}
