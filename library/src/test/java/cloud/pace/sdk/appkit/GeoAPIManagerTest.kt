package cloud.pace.sdk.appkit

import android.location.Location
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.app.api.AppAPI
import cloud.pace.sdk.appkit.utils.CoroutineTestRule
import cloud.pace.sdk.poikit.geo.GeoAPIFeature
import cloud.pace.sdk.poikit.geo.GeoAPIManager
import cloud.pace.sdk.poikit.geo.GeoAPIManagerImpl
import cloud.pace.sdk.poikit.geo.GeoAPIResponse
import cloud.pace.sdk.poikit.geo.Polygon
import cloud.pace.sdk.utils.LocationProvider
import cloud.pace.sdk.utils.SystemManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class GeoAPIManagerTest : KoinTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val polygon = listOf(
        listOf(8.427429, 49.01304015764206),
        listOf(8.427166935031618, 49.013005967255644),
        listOf(8.426944768026093, 49.012908601401435),
        listOf(8.426796322288334, 49.01276288345869),
        listOf(8.426744196943954, 49.01259099797384),
        listOf(8.426796326657078, 49.01241911308242),
        listOf(8.42694477420443, 49.0122733965724),
        listOf(8.427166939400362, 49.0121760321509),
        listOf(8.427429, 49.012141842357934),
        listOf(8.427691060599638, 49.0121760321509),
        listOf(8.42791322579557, 49.0122733965724),
        listOf(8.42806167334292, 49.01241911308242),
        listOf(8.428113803056045, 49.01259099797384),
        listOf(8.428061677711664, 49.01276288345869),
        listOf(8.427913231973907, 49.012908601401435),
        listOf(8.427691064968382, 49.013005967255644),
        listOf(8.427429, 49.01304015764206)
    )

    private val appApi = mockk<AppAPI>(relaxed = true)

    private val testModule = module {
        single { appApi }
        single { mockk<SystemManager>(relaxed = true) }
        single { mockk<LocationProvider>(relaxed = true) }
        single<GeoAPIManager> {
            GeoAPIManagerImpl(get(), get(), get(), coroutineTestRule.testDispatcherProvider)
        }
    }

    private val geoApiManager: GeoAPIManager by inject()

    @Before
    fun setup() {
        mockkObject(PACECloudSDK)
        every { PACECloudSDK.configuration.appsDistanceThresholdInMeters } returns 150

        startKoin {
            modules(testModule)
        }
    }

    @Test
    fun `geo json is requested once at init`() = runTest {
        coEvery { appApi.getGeoApiApps() } returns Result.success(createGeoAPIResponse(listOf(polygon)))

        get<GeoAPIManager>()

        coVerify(exactly = 1) { appApi.getGeoApiApps() }
    }

    @Test
    fun `app is available because location distance is smaller than 150 meters`() = runTest {
        coEvery { appApi.getGeoApiApps() } returns Result.success(createGeoAPIResponse(listOf(polygon)))

        val apps = geoApiManager.apps(49.012713, 8.427777).getOrThrow()

        assertEquals(1, apps.size)
    }

    @Test
    fun `app is not available because location distance is greater than 150 meters`() = runTest {
        coEvery { appApi.getGeoApiApps() } returns Result.success(createGeoAPIResponse(listOf(polygon)))

        val apps = geoApiManager.apps(49.0599842, 8.374426).getOrThrow()

        assertEquals(0, apps.size)
    }

    @Test
    fun `app is not available in 2000 polygons`() = runTest {
        coEvery { appApi.getGeoApiApps() } returns Result.success(get2000PolygonsResponse())

        val apps = geoApiManager.apps(49.405779, 8.6485949).getOrThrow()

        assertEquals(0, apps.size)
    }

    @Test
    fun `cache returns failure when fetching apps`() = runTest {
        val expected = Exception("What a terrible failure")
        coEvery { appApi.getGeoApiApps() } returns Result.failure(expected)

        val exception = geoApiManager.apps(49.012722, 8.427326).exceptionOrNull()

        assertEquals(expected, exception)
    }

    @Test
    fun `cache successfully returns features`() = runTest {
        val id = "e3211b77-03f0-4d49-83aa-4adaa46d95ae"
        coEvery { appApi.getGeoApiApps() } returns Result.success(createGeoAPIResponse(listOf(polygon), id))

        val features = geoApiManager.features(49.012713, 8.427777).getOrThrow()

        assertEquals(1, features.size)
        assertEquals(id, features.first().id)
    }

    @Test
    fun `cache returns failure when fetching features`() = runTest {
        val expected = Exception("What a terrible failure")
        coEvery { appApi.getGeoApiApps() } returns Result.failure(expected)

        val exception = geoApiManager.features(49.012713, 8.427777).exceptionOrNull()

        assertEquals(expected, exception)
    }

    @Test
    fun `poi id is in range`() = runTest {
        val id = "e3211b77-03f0-4d49-83aa-4adaa46d95ae"
        val location = mock(Location::class.java)
        `when`(location.latitude).then { 49.012722 }
        `when`(location.longitude).then { 8.427326 }
        coEvery { appApi.getGeoApiApps() } returns Result.success(createGeoAPIResponse(listOf(polygon), id))

        assertTrue(geoApiManager.isPoiInRange(id, location))
    }

    @Test
    fun `poi id is not in range`() = runTest {
        val id1 = "e3211b77-03f0-4d49-83aa-4adaa46d95ae"
        val id2 = "992b77b6-5982-4848-88fe-ae2633308279"
        val location = mock(Location::class.java)
        `when`(location.latitude).then { 49.012722 }
        `when`(location.longitude).then { 8.427326 }
        coEvery { appApi.getGeoApiApps() } returns Result.success(createGeoAPIResponse(listOf(polygon), id2))

        assertFalse(geoApiManager.isPoiInRange(id1, location))
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun get2000PolygonsResponse(): GeoAPIResponse {
        val reversedPolygon = polygon.reversed()
        val polygons = MutableList(1000) { polygon }
        val reversedPolygons = MutableList(1000) { reversedPolygon }
        val shapes = (polygons + reversedPolygons).shuffled()

        return createGeoAPIResponse(shapes)
    }

    private fun createGeoAPIResponse(shapes: List<List<List<Double>>>, id: String? = null): GeoAPIResponse {
        return GeoAPIResponse(
            "FeatureCollection",
            shapes.map {
                GeoAPIFeature(
                    id ?: UUID.randomUUID().toString(),
                    "Feature",
                    Polygon(listOf(it)),
                    mapOf(
                        "apps" to listOf(
                            "type" to "fueling",
                            "url" to "https://fueling.app.test"
                        )
                    )
                )
            }
        )
    }
}
