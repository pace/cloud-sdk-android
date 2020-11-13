package cloud.pace.sdk.appkit.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import cloud.pace.sdk.utils.Log
import cloud.pace.sdk.utils.SystemManager
import cloud.pace.sdk.utils.onMainThread

interface NetworkChangeListener {

    fun getNetworkChanges(callback: (Boolean) -> Unit)
}

class NetworkChangeListenerImpl(private val systemManager: SystemManager) : NetworkChangeListener {

    override fun getNetworkChanges(callback: (Boolean) -> Unit) {
        val connectivityManager = systemManager.getConnectivityManager()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d("Network available - retry app request")
                onMainThread { callback(true) }
                try {
                    connectivityManager?.unregisterNetworkCallback(this)
                } catch (e: IllegalArgumentException) {
                    // listener was already unregistered
                    Log.e(e, "Exception when unregister NetworkChangeListener: ${e.message}")
                }
            }
        }

        connectivityManager?.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            networkCallback
        )

        systemManager.getHandler().postDelayed({
            try {
                Log.d("Network timeout")
                connectivityManager?.unregisterNetworkCallback(networkCallback)
                callback(false)
            } catch (e: IllegalArgumentException) {
                // listener was already unregistered
                Log.e(e, "Exception when unregister NetworkChangeListener: ${e.message}")
            }
        }, NETWORK_TIMEOUT)
    }

    companion object {
        private const val NETWORK_TIMEOUT = 30 * 1000L // 30sec
    }
}
