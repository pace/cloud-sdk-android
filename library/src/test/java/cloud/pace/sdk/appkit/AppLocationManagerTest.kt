package cloud.pace.sdk.appkit

import android.location.Location
import android.os.Handler
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.location.AppLocationManagerImpl
import cloud.pace.sdk.appkit.utils.NoLocationFound
import cloud.pace.sdk.appkit.utils.TestLocationProvider
import cloud.pace.sdk.appkit.utils.TestSystemManager
import cloud.pace.sdk.utils.*
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@RunWith(MockitoJUnitRunner::class)
class AppLocationManagerTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val nowTimestamp = 1582203600000 // Thu 20.02.2020 14:00:00

    @Before
    fun init() {
        PACECloudSDK.configuration = Configuration("", "", "", "", environment = Environment.DEVELOPMENT, locationAccuracy = 150)
    }

    @Test
    fun `location with best accuracy`() {
        val systemManager = mock(SystemManager::class.java)
        val bestAccuracyTime = nowTimestamp + 1000
        val location = mock(Location::class.java)

        `when`(systemManager.getCurrentTimeMillis())
            .thenReturn(nowTimestamp)
            .thenReturn(bestAccuracyTime)
        `when`(systemManager.getHandler()).thenReturn(mock(Handler::class.java))
        `when`(location.latitude).then { 49.01244 }
        `when`(location.longitude).then { 8.42653 }
        `when`(location.accuracy).then { 20f }
        `when`(location.time).then { bestAccuracyTime }

        val locationProvider = TestLocationProvider(LocationState.LOCATION_HIGH_ACCURACY, location)
        val appLocationListener = AppLocationManagerImpl(locationProvider, systemManager)
        val locationFuture = CompletableFutureCompat<Result<Location>>()
        appLocationListener.start {
            locationFuture.complete(it)
        }

        val result = locationFuture.get(2, TimeUnit.SECONDS)
        assertTrue(result.isSuccess)
        assertEquals(location, result.getOrNull())
    }

    @Test
    fun `location with medium accuracy`() {
        val systemManager = mock(SystemManager::class.java)
        val mediumAccuracyTime = nowTimestamp + 7000
        val location = mock(Location::class.java)

        `when`(systemManager.getCurrentTimeMillis())
            .thenReturn(nowTimestamp)
            .thenReturn(mediumAccuracyTime)
        `when`(systemManager.getHandler()).thenReturn(mock(Handler::class.java))
        `when`(location.latitude).then { 49.01244 }
        `when`(location.longitude).then { 8.42653 }
        `when`(location.accuracy).then { 40f }
        `when`(location.time).then { mediumAccuracyTime }

        val locationProvider = TestLocationProvider(LocationState.LOCATION_HIGH_ACCURACY, location)
        val appLocationListener = AppLocationManagerImpl(locationProvider, systemManager)
        val locationFuture = CompletableFutureCompat<Result<Location>>()
        appLocationListener.start {
            locationFuture.complete(it)
        }

        val result = locationFuture.get(2, TimeUnit.SECONDS)
        assertTrue(result.isSuccess)
        assertEquals(location, result.getOrNull())
    }

    @Test
    fun `location with low accuracy`() {
        val systemManager = mock(SystemManager::class.java)
        val lowAccuracyTime = nowTimestamp + 13000
        val location = mock(Location::class.java)

        `when`(systemManager.getCurrentTimeMillis())
            .thenReturn(nowTimestamp)
            .thenReturn(lowAccuracyTime)
        `when`(systemManager.getHandler()).thenReturn(mock(Handler::class.java))
        `when`(location.latitude).then { 49.01244 }
        `when`(location.longitude).then { 8.42653 }
        `when`(location.accuracy).then { 140f }
        `when`(location.time).then { lowAccuracyTime }

        val locationProvider = TestLocationProvider(LocationState.LOCATION_LOW_ACCURACY, location)
        val appLocationListener = AppLocationManagerImpl(locationProvider, systemManager)
        val locationFuture = CompletableFutureCompat<Result<Location>>()
        appLocationListener.start {
            locationFuture.complete(it)
        }

        val result = locationFuture.get(2, TimeUnit.SECONDS)
        assertTrue(result.isSuccess)
        assertEquals(location, result.getOrNull())
    }

    @Test
    fun `discard inaccurate location`() {
        val systemManager = mock(SystemManager::class.java)
        val location = mock(Location::class.java)

        `when`(systemManager.getHandler()).thenReturn(mock(Handler::class.java))
        `when`(location.accuracy).then { 100f }

        val locationProvider = TestLocationProvider(LocationState.LOCATION_LOW_ACCURACY, location)
        val appLocationListener = AppLocationManagerImpl(locationProvider, systemManager)
        val locationFuture = CompletableFutureCompat<Result<Location>>()
        appLocationListener.start {
            locationFuture.complete(it)
        }

        try {
            locationFuture.get(1, TimeUnit.SECONDS)
        } catch (e: Exception) {
            assertTrue(e is TimeoutException)
        }
    }

    @Test
    fun `discard null location`() {
        val systemManager = mock(SystemManager::class.java)

        `when`(systemManager.getHandler()).thenReturn(mock(Handler::class.java))

        val locationProvider = TestLocationProvider(LocationState.LOCATION_LOW_ACCURACY, null)
        val appLocationListener = AppLocationManagerImpl(locationProvider, systemManager)
        val locationFuture = CompletableFutureCompat<Result<Location>>()
        appLocationListener.start {
            locationFuture.complete(it)
        }

        try {
            locationFuture.get(1, TimeUnit.SECONDS)
        } catch (e: Exception) {
            assertTrue(e is TimeoutException)
        }
    }

    @Test
    fun `discard old location`() {
        val systemManager = mock(SystemManager::class.java)
        val mediumAccuracyTime = nowTimestamp + 7000
        val location = mock(Location::class.java)

        `when`(systemManager.getCurrentTimeMillis())
            .thenReturn(nowTimestamp)
            .thenReturn(mediumAccuracyTime)
            .thenReturn(mediumAccuracyTime + 60000)
        `when`(systemManager.getHandler()).thenReturn(mock(Handler::class.java))
        `when`(location.accuracy).then { 40f }
        `when`(location.time).then { mediumAccuracyTime }

        val locationProvider = TestLocationProvider(LocationState.LOCATION_LOW_ACCURACY, location)
        val appLocationListener = AppLocationManagerImpl(locationProvider, systemManager)
        val locationFuture = CompletableFutureCompat<Result<Location>>()
        appLocationListener.start {
            locationFuture.complete(it)
        }

        try {
            locationFuture.get(1, TimeUnit.SECONDS)
        } catch (e: Exception) {
            assertTrue(e is TimeoutException)
        }
    }

    @Test
    fun `location timeout`() {
        val handler = mock(Handler::class.java)
        val systemManager = TestSystemManager(mockHandler = handler)

        `when`(handler.postDelayed(any(Runnable::class.java), anyLong())).thenAnswer { invocation ->
            (invocation.getArgument(0) as Runnable).run()
            null
        }

        val locationProvider = TestLocationProvider(LocationState.NO_LOCATION_FOUND, null)
        val appLocationListener = AppLocationManagerImpl(locationProvider, systemManager)
        val locationFuture = CompletableFutureCompat<Result<Location>>()
        appLocationListener.start {
            locationFuture.complete(it)
        }

        val result = locationFuture.get()
        val exception = result.exceptionOrNull()
        assertNotNull(result)
        assertTrue(result.isFailure)
        assertNotNull(exception)
        assertEquals(NoLocationFound, exception)
    }
}
