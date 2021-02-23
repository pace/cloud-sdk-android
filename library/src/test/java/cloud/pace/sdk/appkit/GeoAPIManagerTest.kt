package cloud.pace.sdk.appkit

import cloud.pace.sdk.appkit.geo.*
import cloud.pace.sdk.appkit.utils.TestAppAPI
import cloud.pace.sdk.utils.CompletableFutureCompat
import cloud.pace.sdk.utils.SystemManager
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Test
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

    @Test
    fun `app is available because location is inside of the polygon`() {
        val appApi = object : TestAppAPI() {
            override fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
                completion(Result.success(createGeoAPIResponse(listOf(polygon))))
            }
        }

        val geoApiManager = GeoAPIManagerImpl(appApi, mock(SystemManager::class.java))
        val gasStationFuture = CompletableFutureCompat<List<GeoGasStation>>()
        geoApiManager.apps(49.012713, 8.427777) {
            it.onSuccess { gasStationFuture.complete(it) }
            it.onFailure { throw it }
        }

        val gasStation = gasStationFuture.get(2, TimeUnit.SECONDS)
        assertEquals(1, gasStation.size)
    }

    @Test
    fun `app is available because location is inside on the edge of the polygon`() {
        val appApi = object : TestAppAPI() {
            override fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
                completion(Result.success(createGeoAPIResponse(listOf(polygon))))
            }
        }

        val geoApiManager = GeoAPIManagerImpl(appApi, mock(SystemManager::class.java))
        val gasStationFuture = CompletableFutureCompat<List<GeoGasStation>>()
        geoApiManager.apps(49.012679, 8.426794) {
            it.onSuccess { gasStationFuture.complete(it) }
            it.onFailure { throw it }
        }

        val gasStation = gasStationFuture.get(2, TimeUnit.SECONDS)
        assertEquals(1, gasStation.size)
    }

    @Test
    fun `app is not available because location is outside of the polygon`() {
        val appApi = object : TestAppAPI() {
            override fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
                completion(Result.success(createGeoAPIResponse(listOf(polygon))))
            }
        }

        val geoApiManager = GeoAPIManagerImpl(appApi, mock(SystemManager::class.java))
        val gasStationFuture = CompletableFutureCompat<List<GeoGasStation>>()
        geoApiManager.apps(49.011819, 8.426067) {
            it.onSuccess { gasStationFuture.complete(it) }
            it.onFailure { throw it }
        }

        val gasStation = gasStationFuture.get(2, TimeUnit.SECONDS)
        assertEquals(0, gasStation.size)
    }

    @Test
    fun `app is not available because location is outside on the edge of the polygon`() {
        val appApi = object : TestAppAPI() {
            override fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
                completion(Result.success(createGeoAPIResponse(listOf(polygon))))
            }
        }

        val geoApiManager = GeoAPIManagerImpl(appApi, mock(SystemManager::class.java))
        val gasStationFuture = CompletableFutureCompat<List<GeoGasStation>>()
        geoApiManager.apps(49.012716, 8.426764) {
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

        val geoApiManager = GeoAPIManagerImpl(appApi, mock(SystemManager::class.java))
        val gasStationFuture = CompletableFutureCompat<List<GeoGasStation>>()
        geoApiManager.apps(49.012716, 8.426764) {
            it.onSuccess { gasStationFuture.complete(it) }
            it.onFailure { throw it }
        }

        val gasStation = gasStationFuture.get(2, TimeUnit.SECONDS)
        assertEquals(0, gasStation.size)
    }

    @Test
    fun `cache returns failure`() {
        val appApi = object : TestAppAPI() {
            override fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
                completion(Result.failure(RuntimeException()))
            }
        }

        val geoApiManager = GeoAPIManagerImpl(appApi, mock(SystemManager::class.java))
        val exceptionFuture = CompletableFutureCompat<Throwable?>()
        geoApiManager.apps(49.012722, 8.427326) {
            it.onSuccess { exceptionFuture.complete(null) }
            it.onFailure { exceptionFuture.complete(it) }
        }

        assertNotNull(exceptionFuture.get(2, TimeUnit.SECONDS))
    }

    private fun get2000PolygonsResponse(): GeoAPIResponse {
        val reversedPolygon = polygon.reversed()
        val polygons = MutableList(1000) { polygon }
        val reversedPolygons = MutableList(1000) { reversedPolygon }
        val shapes = (polygons + reversedPolygons).shuffled()

        return createGeoAPIResponse(shapes)
    }

    private fun createGeoAPIResponse(shapes: List<List<List<Double>>>): GeoAPIResponse {
        return GeoAPIResponse(
            "FeatureCollection",
            shapes.map {
                GeoAPIFeature(
                    UUID.randomUUID().toString(),
                    "Feature",
                    GeoAPIGeometry(
                        "Polygon",
                        listOf(it)
                    ),
                    GeoAPIProperties(
                        listOf(
                            GeoAPIApp(
                                "fueling",
                                "https://fueling.app.test"
                            )
                        )
                    )
                )
            }
        )
    }
}
