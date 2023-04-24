package cloud.pace.sdk.appkit

import cloud.pace.sdk.api.fueling.generated.model.FuelPrice
import cloud.pace.sdk.api.utils.RequestUtils.LOCATION_HEADER
import cloud.pace.sdk.poikit.poi.Day
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.LocationPoint
import cloud.pace.sdk.poikit.poi.OpeningHour
import cloud.pace.sdk.poikit.poi.OpeningHours
import cloud.pace.sdk.poikit.poi.OpeningRule
import cloud.pace.sdk.poikit.poi.Price
import cloud.pace.sdk.poikit.poi.tiles.POIAPI
import cloud.pace.sdk.poikit.poi.tiles.TilesAPIManager
import cloud.pace.sdk.poikit.poi.tiles.TilesAPIManagerImpl
import cloud.pace.sdk.poikit.utils.LatLngBounds
import cloud.pace.sdk.poikit.utils.toVisibleRegion
import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import retrofit2.Response
import java.net.HttpURLConnection
import java.util.Date
import java.util.UUID
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TilesAPIManagerTest : KoinTest {

    private val poiAPI = mockk<POIAPI>(relaxed = true)
    private val gasStations = listOf(createGasStation(49.10, 8.30), createGasStation(49.20, 8.50), createGasStation(49.30, 8.60))
    private val tilesAPIManager: TilesAPIManager by inject()

    private val testModule = module {
        single { poiAPI }
        single<TilesAPIManager> {
            TilesAPIManagerImpl(poiAPI)
        }
    }

    @Before
    fun init() {
        stopKoin()

        coEvery { poiAPI.getTiles(any()) } returns gasStations
        coEvery { poiAPI.getGasStation(any(), any()) } returns gasStationResponse(gasStations[0]) andThen gasStationResponse(gasStations[1]) andThen gasStationResponse(gasStations[2])

        startKoin {
            modules(testModule)
        }
    }

    @Test
    fun `get tiles returns gas stations within visible region`() = runTest {
        val visibleRegion = LatLngBounds(LatLng(49.01, 8.40), 20.000).toVisibleRegion()

        val result = tilesAPIManager.getTiles(visibleRegion, 8.0).getOrNull()
        assertEquals(gasStations, result)
    }

    @Test
    fun `get tiles by visible region returns failure`() = runTest {
        val visibleRegion = LatLngBounds(LatLng(49.01, 8.40), 20.000).toVisibleRegion()
        val expected = Exception("Test exception")
        coEvery { poiAPI.getTiles(any()) } throws expected

        val result = tilesAPIManager.getTiles(visibleRegion, 8.0).exceptionOrNull()
        assertEquals(expected, result)
    }

    @Test
    fun `get tiles returns gas stations for specific IDs`() = runTest {
        val ids = gasStations.map(GasStation::id)

        val result = tilesAPIManager.getTiles(ids).getOrNull()
        assertEquals(gasStations, result)
    }

    @Test
    fun `get tiles by IDs returns failure`() = runTest {
        val ids = gasStations.map(GasStation::id)
        val expected = Exception("Test exception")
        coEvery { poiAPI.getTiles(any()) } throws expected

        val result = tilesAPIManager.getTiles(ids).exceptionOrNull()
        assertEquals(expected, result)
    }

    @Test
    fun `gas stations are filtered by ID if API returns more gas stations`() = runTest {
        val ids = gasStations.map(GasStation::id)
        val additionalGasStations = listOf(createGasStation(49.10, 8.30), createGasStation(49.20, 8.50))
        coEvery { poiAPI.getTiles(any()) } returns gasStations + additionalGasStations
        coEvery { poiAPI.getGasStation(any(), any()) } returns
            gasStationResponse(gasStations[0]) andThen
            gasStationResponse(gasStations[1]) andThen
            gasStationResponse(gasStations[2]) andThen
            gasStationResponse(additionalGasStations[0]) andThen
            gasStationResponse(additionalGasStations[1])

        val result = tilesAPIManager.getTiles(ids).getOrNull()
        assertEquals(gasStations, result)
    }

    @Test
    fun `get tiles returns gas stations for specific locations`() = runTest {
        val locations = gasStations.associate { it.id to LocationPoint(it.latitude!!, it.longitude!!) }

        val result = tilesAPIManager.getTiles(locations).getOrNull()
        assertEquals(gasStations, result)
    }

    @Test
    fun `get tiles by locations returns failure`() = runTest {
        val locations = gasStations.associate { it.id to LocationPoint(it.latitude!!, it.longitude!!) }
        val expected = Exception("Test exception")
        coEvery { poiAPI.getTiles(any()) } throws expected

        val result = tilesAPIManager.getTiles(locations).exceptionOrNull()
        assertEquals(expected, result)
    }

    @Test
    fun `get tiles returns gas station for specific ID`() = runTest {
        val expected = gasStations.first()

        val result = tilesAPIManager.getTiles(expected.id).getOrNull()
        assertEquals(expected, result)
    }

    @Test
    fun `get tiles by ID returns failure`() = runTest {
        val expected = Exception("Test exception")
        coEvery { poiAPI.getTiles(any()) } throws expected

        val result = tilesAPIManager.getTiles(gasStations.first().id).exceptionOrNull()
        assertEquals(expected, result)
    }

    @Test
    fun `new gas station is fetched if old gas station is moved`() = runTest {
        val oldId = UUID.randomUUID().toString()
        val expected = gasStations.first()
        coEvery { poiAPI.getGasStation(any(), any()) } returns movedResponse(expected.id) andThen gasStationResponse(expected)

        val result = tilesAPIManager.getTiles(oldId).getOrNull()
        assertEquals(expected, result)
    }

    @Test
    fun `max 3 get gas station redirects`() = runTest {
        val oldId = UUID.randomUUID().toString()
        coEvery { poiAPI.getGasStation(any(), any()) } returns movedResponse(oldId)

        val result = tilesAPIManager.getTiles(oldId).exceptionOrNull()
        assert(result is Throwable)
    }

    @Test
    fun `successful gas stations are still returned if another gas station request fails`() = runTest {
        val ids = gasStations.map(GasStation::id)
        coEvery { poiAPI.getGasStation(ids[0], any()) } returns gasStationResponse(gasStations[0])
        coEvery { poiAPI.getGasStation(ids[1], any()) } returns movedResponse(gasStations[1].id)
        coEvery { poiAPI.getGasStation(ids[2], any()) } returns gasStationResponse(gasStations[2])

        val result = tilesAPIManager.getTiles(ids).getOrNull()
        assertEquals(listOf(gasStations[0], gasStations[2]), result)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun createGasStation(
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        id: String = UUID.randomUUID().toString()
    ): GasStation {
        return GasStation(id, arrayListOf()).apply {
            this.id = id
            this.name = "PACE"
            openingHours = listOf(
                OpeningHours(listOf(Day.FRIDAY, Day.MONDAY, Day.THURSDAY, Day.TUESDAY, Day.WEDNESDAY), listOf(OpeningHour("8", "22")), OpeningRule.OPEN),
                OpeningHours(listOf(Day.SUNDAY), listOf(OpeningHour("0", "0")), OpeningRule.CLOSED),
                OpeningHours(listOf(Day.SATURDAY), listOf(OpeningHour("10", "20")), OpeningRule.OPEN)
            )
            prices = mutableListOf(
                Price(FuelPrice.FuelType.RON95E5.value, "Super", 1.453),
                Price(FuelPrice.FuelType.RON95E10.value, "Super E10", 1.349),
                Price(FuelPrice.FuelType.DIESEL.value, "Diesel", 1.289)
            )
            currency = "EUR"
            priceFormat = "d.dds"
            isOnlineCoFuGasStation = true
            updatedAt = Date(1575651009687)
            this.latitude = latitude
            this.longitude = longitude
            cofuPaymentMethods = mutableListOf("paypal, giropay, creditcard")
        }
    }

    private fun gasStationResponse(gasStation: GasStation): Response<cloud.pace.sdk.api.poi.generated.model.GasStation> {
        return Response.success(
            cloud.pace.sdk.api.poi.generated.model.GasStation().apply {
                id = gasStation.id
                latitude = gasStation.latitude?.toFloat()
                longitude = gasStation.longitude?.toFloat()
            }
        )
    }

    private fun movedResponse(newId: String): Response<cloud.pace.sdk.api.poi.generated.model.GasStation> {
        return Response.error(
            "Moved".toResponseBody(),
            okhttp3.Response.Builder()
                .code(HttpURLConnection.HTTP_MOVED_PERM)
                .protocol(Protocol.HTTP_2)
                .message("Moved")
                .addHeader(LOCATION_HEADER, "/$newId")
                .request(Request.Builder().url("https://test.url").build())
                .build()
        )
    }
}
