package cloud.pace.sdk.app

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cloud.pace.sdk.api.pay.generated.model.Transactions
import cloud.pace.sdk.app.databinding.ActivityMainBinding
import cloud.pace.sdk.app.ui.theme.Screen
import cloud.pace.sdk.app.view.mainscreen.BottomBar
import cloud.pace.sdk.app.view.mainscreen.SuccessfulLoginDialog
import cloud.pace.sdk.app.view.mainscreen.dashboardscreen.DashboardScreen
import cloud.pace.sdk.app.view.mainscreen.listscreen.ListScreen
import cloud.pace.sdk.app.view.mainscreen.settings.SettingsScreen
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.viewBinding
import kotlinx.coroutines.launch

class MainScreenActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)
    private val gasStations = mutableStateOf<List<GasStation>>(emptyList())
    private val transactions = mutableStateOf<Transactions>(emptyList())
    private var currentApps by mutableStateOf(listOf<App>())
    private val lastLocation = MutableLiveData<Location>()
    private val defaultAppCallback = object : AppCallbackImpl() {}
    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    private val permissionsGranted = MutableLiveData<Boolean>()
    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (permissions.all { permission -> it[permission] == true }) {
            startLocationListener()
            permissionsGranted.value = true
        } else {
            permissionsGranted.value = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BottomBarController()
                // Dialog that asks the user if he wants to activate the BioAuth, if he agrees the BioAuth will be enabled.
                if (!biometryStatus.value) {
                    SuccessfulLoginDialog(this@MainScreenActivity) {
                        lifecycleScope.launch {
                            IDKit.enableBiometricAuthentication {
                                when (it) {
                                    is Success -> Toast.makeText(this@MainScreenActivity, if (it.result) "Biometric authentication set" else "Biometric authentication not set", Toast.LENGTH_SHORT)
                                        .show()
                                    is Failure -> Toast.makeText(this@MainScreenActivity, it.throwable.toString(), Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            }
        }

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

        if (permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
            startLocationListener()
            permissionsGranted.value = true
        } else {
            permissionsGranted.value = false
        }

        IDKit.getTransactions {
            when (it) {
                is Success -> transactions.value = it.result.take(10)
                is Failure -> Toast.makeText(this@MainScreenActivity, it.throwable.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startLocationListener() {
        POIKit.startLocationListener().location.observe(this) {
            val location = lastLocation.value
            if (location == null || location.distanceTo(it) > APP_DISTANCE_THRESHOLD) {
                AppKit.requestLocalApps { completion ->
                    if (completion is Success) {
                        val apps = completion.result
                        // Only refresh App Drawers if old and new app lists are not equal
                        if (currentApps.size != apps.size || !currentApps.containsAll(apps)) {
                            currentApps = apps
                            AppKit.openApps(this, completion.result, binding.mainLayout, bottomMargin = 100f, callback = defaultAppCallback)
                        }
                    }
                }
                requestCoFuGasStations(it)
                lastLocation.value = it
            }
        }
    }

    companion object {
        private const val APP_DISTANCE_THRESHOLD = 15
    }

    @Composable
    fun BottomBarController() {
        val navController = rememberNavController()
        Scaffold(
            bottomBar = {
                BottomBar(
                    navController
                )
            }
        )
        { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding))
            BottomBarControl(navController)
        }
    }

    @Composable
    fun BottomBarControl(navController: NavHostController) {

        val transactions = remember { transactions }

        val gasStations = remember { gasStations }

        NavHost(navController, startDestination = Screen.List.route) {
            composable(Screen.List.route) {
                ListScreen(gasStations.value, lastLocation, permissionsGranted)
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(transactions.value) {
                    AppKit.openPaymentApp(this@MainScreenActivity, true, callback = defaultAppCallback)
                }
            }

            composable(Screen.Settings.route) {
                SettingsScreen(this@MainScreenActivity, lifecycleScope) {
                    finish()
                }
            }
        }
    }

    private fun requestCoFuGasStations(location: Location) {
        val radius = 10000
        if (radius == null) {
            Toast.makeText(this@MainScreenActivity, "The radius is not a valid representation of a number", Toast.LENGTH_SHORT).show()
        } else {
            POIKit.requestCofuGasStations(location, radius) {
                when (it) {
                    is Success -> {
                        gasStations.value = it.result
                    }
                    is Failure -> {
                        Toast.makeText(this@MainScreenActivity, it.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}

