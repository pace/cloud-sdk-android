package cloud.pace.sdk.appkit

import cloud.pace.sdk.appkit.app.api.AppAPI
import cloud.pace.sdk.appkit.app.api.AppAPIImpl
import cloud.pace.sdk.poikit.geo.GeoAPIClient
import cloud.pace.sdk.poikit.geo.GeoAPIFeature
import cloud.pace.sdk.poikit.geo.GeoAPIResponse
import cloud.pace.sdk.poikit.geo.Polygon
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import retrofit2.Response
import java.util.UUID

class AppAPITest : KoinTest {

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

    private val geoAPIClient = mockk<GeoAPIClient>(relaxed = true).also {
        coEvery { it.getGeoApiApps() } returns Response.success(createGeoAPIResponse(listOf(polygon)))
    }

    private val testModule = module {
        single { geoAPIClient }
        single<AppAPI> { AppAPIImpl(get()) }
    }

    private val appApi: AppAPI by inject()

    @Before
    fun setup() {
        startKoin {
            modules(testModule)
        }
    }

    @Test
    fun `only one api request if geo json is requested multiple times simultaneously`() = runTest {
        val request1 = async { appApi.getGeoApiApps() }
        val request2 = async { appApi.getGeoApiApps() }

        request1.await()
        request2.await()

        coVerify(exactly = 1) { geoAPIClient.getGeoApiApps() }
    }

    @After
    fun tearDown() {
        stopKoin()
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
