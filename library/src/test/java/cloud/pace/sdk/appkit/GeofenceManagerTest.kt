package cloud.pace.sdk.appkit

import android.app.PendingIntent
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cloud.pace.sdk.appkit.geofences.GeofenceCallback
import cloud.pace.sdk.appkit.geofences.GeofenceManager
import cloud.pace.sdk.appkit.geofences.GeofenceManagerImpl
import cloud.pace.sdk.utils.AppKitKoinComponent
import cloud.pace.sdk.utils.KoinConfig
import com.google.android.gms.location.GeofencingClient
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GeofenceManagerTest : AppKitKoinComponent {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var geofencingClient: GeofencingClient

    @Mock
    private lateinit var geofenceCallback: GeofenceCallback

    @Mock
    private lateinit var pendingIntent: PendingIntent

    @Before
    fun setup() {
        mockkStatic(PendingIntent::class)
        every { PendingIntent.getBroadcast(any(), any(), any(), any()) } returns pendingIntent
    }

    @After
    fun onFinished() = stopKoin()

    private fun setupKoinForTests(module: Module) {
        KoinConfig.setupForTests(mockContext, module)
    }

    @Test
    fun `does not create invalid request on empty list`() {
        val geofenceManager = GeofenceManagerImpl()
        setupKoinForTests(module {
            single { geofencingClient }
            single { geofenceCallback }
            single { geofenceManager }
        })

        geofenceManager.enable(listOf(), {}, {})

        verify(geofencingClient, times(0)).addGeofences(any(), any())
    }

    @Test
    fun `single request`() {
        val geofenceManager = GeofenceManagerImpl()
        setupKoinForTests(module {
            single { geofencingClient }
            single { geofenceCallback }
            single { geofenceManager }
        })

        geofenceManager.enable(listOf(GeofenceManager.GeofenceLocation(49.0, 8.0, 100f, "0", "tag")), {}, {})

        verify(geofencingClient, times(1)).removeGeofences(any<PendingIntent>())
        verify(geofencingClient, times(1)).addGeofences(ArgumentMatchers.argThat {
            it.geofences.size == 1 &&
                it.geofences.first().requestId == "0"
        }, any())
    }

    @Test
    fun `request with multiple locations`() {
        val geofenceManager = GeofenceManagerImpl()
        setupKoinForTests(module {
            single { geofencingClient }
            single { geofenceCallback }
            single { geofenceManager }
        })

        geofenceManager.enable(listOf(GeofenceManager.GeofenceLocation(49.0, 8.0, 100f, "0", "tag"), GeofenceManager.GeofenceLocation(50.0, 9.0, 100f, "1", "tag")), {}, {})

        verify(geofencingClient, times(1)).removeGeofences(any<PendingIntent>())
        verify(geofencingClient, times(1)).addGeofences(ArgumentMatchers.argThat {
            it.geofences.size == 2 &&
                it.geofences.find { it.requestId == "0" } != null && it.geofences.find { it.requestId == "1" } != null
        }, any())
    }

    @Test
    fun `disabling removes requests`() {
        val geofenceManager = GeofenceManagerImpl()
        setupKoinForTests(module {
            single { geofencingClient }
            single { geofenceCallback }
            single { geofenceManager }
        })

        geofenceManager.disable()

        verify(geofencingClient, times(1)).removeGeofences(any<PendingIntent>())
    }
}
