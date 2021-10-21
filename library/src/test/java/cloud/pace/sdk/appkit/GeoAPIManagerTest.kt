package cloud.pace.sdk.appkit

import android.location.Location
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.utils.TestAppAPI
import cloud.pace.sdk.poikit.geo.*
import cloud.pace.sdk.utils.*
import junit.framework.Assert.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*
import java.util.concurrent.TimeUnit

class GeoAPIManagerTest {

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

    @Before
    fun init() {
        PACECloudSDK.configuration = Configuration("", "", "", "", environment = Environment.DEVELOPMENT, oidConfiguration = null)
    }

    @Test
    fun `app is available because location distance is smaller than 150 meters`() {
        val appApi = object : TestAppAPI() {
            override fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
                completion(Result.success(createGeoAPIResponse(listOf(polygon))))
            }
        }

        val geoApiManager = GeoAPIManagerImpl(appApi, mock(SystemManager::class.java), mock(LocationProvider::class.java))
        val gasStationFuture = CompletableFutureCompat<List<GeoGasStation>>()
        geoApiManager.apps(49.012713, 8.427777) {
            it.onSuccess { gasStationFuture.complete(it) }
            it.onFailure { throw it }
        }

        val gasStation = gasStationFuture.get(2, TimeUnit.SECONDS)
        assertEquals(1, gasStation.size)
    }

    @Test
    fun `app is not available because location distance is greater than 150 meters`() {
        val appApi = object : TestAppAPI() {
            override fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
                completion(Result.success(createGeoAPIResponse(listOf(polygon))))
            }
        }

        val geoApiManager = GeoAPIManagerImpl(appApi, mock(SystemManager::class.java), mock(LocationProvider::class.java))
        val gasStationFuture = CompletableFutureCompat<List<GeoGasStation>>()
        geoApiManager.apps(49.0599842, 8.374426) {
            it.onSuccess { gasStationFuture.complete(it) }
            it.onFailure { throw it }
        }

        val gasStation = gasStationFuture.get(2, TimeUnit.SECONDS)
        assertEquals(0, gasStation.size)
    }

    @Test
    fun `app is not available in 2000 polygons`() {
        val appApi = object : TestAppAPI() {
            override fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
                completion(Result.success(get2000PolygonsResponse()))
            }
        }

        val geoApiManager = GeoAPIManagerImpl(appApi, mock(SystemManager::class.java), mock(LocationProvider::class.java))
        val gasStationFuture = CompletableFutureCompat<List<GeoGasStation>>()
        geoApiManager.apps(49.405779, 8.6485949) {
            it.onSuccess { gasStationFuture.complete(it) }
            it.onFailure { throw it }
        }

        val gasStation = gasStationFuture.get(2, TimeUnit.SECONDS)
        assertEquals(0, gasStation.size)
    }

    @Test
    fun `cache returns failure when fetching apps`() {
        val exception = Exception("What a terrible failure")

        val appApi = object : TestAppAPI() {
            override fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
                completion(Result.failure(exception))
            }
        }

        val geoApiManager = GeoAPIManagerImpl(appApi, mock(SystemManager::class.java), mock(LocationProvider::class.java))
        val exceptionFuture = CompletableFutureCompat<Throwable?>()
        geoApiManager.apps(49.012722, 8.427326) {
            it.onSuccess { exceptionFuture.complete(null) }
            it.onFailure { exceptionFuture.complete(it) }
        }

        assertEquals(exception, exceptionFuture.get(2, TimeUnit.SECONDS))
    }

    @Test
    fun `cache successfully returns features`() {
        val id = "e3211b77-03f0-4d49-83aa-4adaa46d95ae"

        val appApi = object : TestAppAPI() {
            override fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
                completion(Result.success(createGeoAPIResponse(listOf(polygon), id)))
            }
        }

        val geoApiManager = GeoAPIManagerImpl(appApi, mock(SystemManager::class.java), mock(LocationProvider::class.java))
        val geoAPIFeature = CompletableFutureCompat<List<GeoAPIFeature>>()
        geoApiManager.features(id, 49.012713, 8.427777) {
            it.onSuccess { geoAPIFeature.complete(it) }
            it.onFailure { throw it }
        }

        val features = geoAPIFeature.get(2, TimeUnit.SECONDS)
        assertEquals(1, features.size)
        assertEquals(id, features.first().id)
    }

    @Test
    fun `cache returns failure when fetching features`() {
        val id = "e3211b77-03f0-4d49-83aa-4adaa46d95ae"
        val exception = Exception("What a terrible failure")

        val appApi = object : TestAppAPI() {
            override fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
                completion(Result.failure(exception))
            }
        }

        val geoApiManager = GeoAPIManagerImpl(appApi, mock(SystemManager::class.java), mock(LocationProvider::class.java))
        val exceptionFuture = CompletableFutureCompat<Throwable?>()
        geoApiManager.features(id, 49.012713, 8.427777) {
            it.onSuccess { exceptionFuture.complete(null) }
            it.onFailure { exceptionFuture.complete(it) }
        }

        assertEquals(exception, exceptionFuture.get(2, TimeUnit.SECONDS))
    }

    @Test
    fun `poi id is in range`() = runBlocking {
        val id = "e3211b77-03f0-4d49-83aa-4adaa46d95ae"
        val location = mock(Location::class.java)
        `when`(location.latitude).then { 49.012722 }
        `when`(location.longitude).then { 8.427326 }

        val appApi = object : TestAppAPI() {
            override fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
                completion(Result.success(createGeoAPIResponse(listOf(polygon), id)))
            }
        }

        val geoApiManager = GeoAPIManagerImpl(appApi, mock(SystemManager::class.java), mock(LocationProvider::class.java))

        assertTrue(geoApiManager.isPoiInRange(id, location))
    }

    @Test
    fun `poi id is not in range`() = runBlocking {
        val id1 = "e3211b77-03f0-4d49-83aa-4adaa46d95ae"
        val id2 = "992b77b6-5982-4848-88fe-ae2633308279"
        val location = mock(Location::class.java)
        `when`(location.latitude).then { 49.012722 }
        `when`(location.longitude).then { 8.427326 }

        val appApi = object : TestAppAPI() {
            override fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
                completion(Result.success(createGeoAPIResponse(listOf(polygon), id2)))
            }
        }

        val geoApiManager = GeoAPIManagerImpl(appApi, mock(SystemManager::class.java), mock(LocationProvider::class.java))

        assertFalse(geoApiManager.isPoiInRange(id1, location))
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
