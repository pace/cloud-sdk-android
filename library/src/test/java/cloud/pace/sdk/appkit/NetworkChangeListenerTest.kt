package cloud.pace.sdk.appkit

import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Handler
import cloud.pace.sdk.appkit.network.NetworkChangeListenerImpl
import cloud.pace.sdk.utils.CompletableFutureCompat
import cloud.pace.sdk.utils.SystemManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class NetworkChangeListenerTest {

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(newSingleThreadContext("UI thread"))
    }

    @Test
    fun `get network change`() {
        val systemManager = mockk<SystemManager>(relaxed = true)
        val connectivityManager = mockk<ConnectivityManager>(relaxed = true)

        every { systemManager.getConnectivityManager() } returns connectivityManager

        every {
            connectivityManager.registerNetworkCallback(any<NetworkRequest>(), any<ConnectivityManager.NetworkCallback>())
        } answers {
            secondArg<ConnectivityManager.NetworkCallback>().onAvailable(mockk())
        }

        val networkChangeListener = NetworkChangeListenerImpl(systemManager)
        val networkChangedFuture = CompletableFutureCompat<Boolean>()
        val callback: (Boolean) -> Unit = {
            networkChangedFuture.complete(it)
        }
        networkChangeListener.getNetworkChanges(callback)

        assertTrue(networkChangedFuture.get(1, TimeUnit.SECONDS))
        verify { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) }
    }

    @Test
    fun `network timeout`() {
        val systemManager = mockk<SystemManager>(relaxed = true)
        val connectivityManager = mockk<ConnectivityManager>(relaxed = true)

        every { systemManager.getConnectivityManager() } returns connectivityManager

        val mockHandler = mockk<Handler>()
        every {
            mockHandler.postDelayed(any(), any())
        } answers {
            firstArg<Runnable>().run()
            true
        }

        every { systemManager.getHandler() } returns mockHandler

        val networkChangeListener = NetworkChangeListenerImpl(systemManager)
        val networkChangedFuture = CompletableFutureCompat<Boolean>()
        val callback: (Boolean) -> Unit = {
            networkChangedFuture.complete(it)
        }
        networkChangeListener.getNetworkChanges(callback)

        assertFalse(networkChangedFuture.get(1, TimeUnit.SECONDS))
        verify { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) }
    }
}
