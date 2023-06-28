package cloud.pace.sdk.app

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import cloud.pace.sdk.api.pay.generated.model.Transactions
import cloud.pace.sdk.app.ui.components.App
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.ui.theme.PACETheme
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success

class MainScreenActivity : AppCompatActivity() {

    private var gasStations by mutableStateOf<List<GasStation>>(emptyList())
    private var transactions by mutableStateOf<Transactions>(emptyList())
    private var apps by mutableStateOf<List<App>?>(null)
    private var lastLocation by mutableStateOf<Location?>(null)
    private var permissionsGranted by mutableStateOf(false)

    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissionsGranted = if (permissions.all { permission -> it[permission] == true }) {
            startLocationListener()
            true
        } else {
            false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PACETheme {
                App(
                    activity = this,
                    lifecycleScope = lifecycleScope,
                    transactions = transactions,
                    gasStations = gasStations,
                    location = lastLocation,
                    permissionsGranted = permissionsGranted
                )
            }
        }

        IDKit.refreshToken()

        biometryStatus.value = IDKit.isBiometricAuthenticationEnabled()

        // Since target SDK 31 (Android 12) ACCESS_FINE_LOCATION must be requested with ACCESS_COARSE_LOCATION
        if (permissions.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissions.launch(permissions)
        } else {
            startLocationListener()
        }
    }

    override fun onResume() {
        super.onResume()

        permissionsGranted = if (permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
            startLocationListener()
            true
        } else {
            false
        }

        IDKit.getTransactions {
            when (it) {
                is Success -> transactions = it.result.take(10)
                is Failure -> Toast.makeText(this@MainScreenActivity, it.throwable.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startLocationListener() {
        POIKit.startLocationListener().location.observe(this) {
            val location = lastLocation
            if (location == null || location.distanceTo(it) > APP_DISTANCE_THRESHOLD) {
                requestCoFuGasStations(it)
                lastLocation = it
            }
        }
    }

    private fun requestCoFuGasStations(location: Location) {
        val radius = 10000
        POIKit.requestCofuGasStations(location, radius) {
            when (it) {
                is Success -> gasStations = it.result
                is Failure -> Toast.makeText(this@MainScreenActivity, it.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val APP_DISTANCE_THRESHOLD = 15
    }
}
