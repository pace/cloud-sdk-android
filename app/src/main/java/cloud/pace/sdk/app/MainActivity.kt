package cloud.pace.sdk.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.api.geo.ConnectedFuelingStatus
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.OIDConfiguration
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var lastLocation: Location? = null
    private var currentApps: List<App> = emptyList()

    private val defaultAppCallback = object : AppCallbackImpl() {
        override fun onLogin(context: Context, result: Completion<String?>) {
            when (result) {
                is Success -> {
                    AlertDialog.Builder(context)
                        .setTitle("Biometric authentication")
                        .setMessage("Do you want to use biometric authentication to authorize payments?")
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton("Yes") { dialog, _ ->
                            IDKit.enableBiometricAuthentication {
                                when (it) {
                                    is Success -> Toast.makeText(context, if (it.result) "Biometric authentication set" else "Biometric authentication not set", Toast.LENGTH_SHORT).show()
                                    is Failure -> Toast.makeText(context, it.throwable.toString(), Toast.LENGTH_LONG).show()
                                }
                            }
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
                is Failure -> Toast.makeText(context, result.throwable.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        IDKit.setup(this, OIDConfiguration.development(clientId = "cloud-sdk-example-app", redirectUri = "pace://cloud-sdk-example"))

        PACECloudSDK.setup(
            this, Configuration(
                clientAppName = "PACECloudSDKExample",
                clientAppVersion = BuildConfig.VERSION_NAME,
                clientAppBuild = BuildConfig.VERSION_CODE.toString(),
                apiKey = "YOUR_API_KEY",
                environment = Environment.DEVELOPMENT,
                domainACL = listOf("pace.cloud"),
                geoAppsScope = "pace-drive-android"
            )
        )

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    startLocationListener()
                }
            }.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            startLocationListener()
        }

        val coFuGasStationsAdapter = CoFuGasStationsAdapter {
            AppKit.openFuelingApp(this, it, false, callback = defaultAppCallback)
        }
        cofu_gas_stations_list.adapter = coFuGasStationsAdapter
        cofu_gas_stations_list.layoutManager = GridLayoutManager(this, 2)

        payment_app.setOnClickListener {
            AppKit.openPaymentApp(this, callback = defaultAppCallback)
        }

        fueling_app.setOnClickListener {
            AppKit.openFuelingApp(this, callback = defaultAppCallback)
        }

        transactions_app.setOnClickListener {
            AppKit.openTransactions(this, callback = defaultAppCallback)
        }

        pace_id_app.setOnClickListener {
            AppKit.openPaceID(this, callback = defaultAppCallback)
        }

        pwa_simulator_app.setOnClickListener {
            AppKit.openAppActivity(this, "https://pwa-simulator.dev.k8s.pacelink.net/", true, defaultAppCallback)
        }

        is_poi_in_range.setOnClickListener {
            val poiId = poi_id.text.toString()
            if (poiId.isBlank()) {
                Toast.makeText(this, "POI ID must not be empty", Toast.LENGTH_SHORT).show()
            } else {
                val start = System.currentTimeMillis()
                lifecycleScope.launch {
                    val isPoiInRange = AppKit.isPoiInRange(poiId)
                    val elapsedTime = System.currentTimeMillis() - start
                    Toast.makeText(this@MainActivity, "Is POI in range result is $isPoiInRange and took $elapsedTime ms", Toast.LENGTH_LONG).show()
                }
            }
        }

        request_cofu_gas_stations.setOnClickListener {
            val location = lastLocation
            if (location == null) {
                Toast.makeText(this, "No location found", Toast.LENGTH_SHORT).show()
            } else {
                val radius = radius.text.toString().toIntOrNull()
                if (radius == null) {
                    Toast.makeText(this, "The radius is not a valid representation of a number", Toast.LENGTH_SHORT).show()
                } else {
                    request_cofu_gas_stations.isEnabled = false
                    AppKit.requestCofuGasStations(location, radius) {
                        request_cofu_gas_stations.isEnabled = true
                        when (it) {
                            is Success -> {
                                coFuGasStationsAdapter.entries = it.result
                                empty_view.isVisible = it.result.isEmpty()
                            }
                            is Failure -> {
                                coFuGasStationsAdapter.entries = emptyList()
                                empty_view.isVisible = true
                            }
                        }
                    }
                }
            }
        }

        user_info.setOnClickListener {
            if (IDKit.isAuthorizationValid()) {
                IDKit.refreshToken { completion ->
                    showUserEmail(completion)
                }
            } else {
                lifecycleScope.launch {
                    IDKit.authorize(this@MainActivity) { completion ->
                        showUserEmail(completion)
                    }
                }
            }
        }

        discover_configuration.setOnClickListener {
            IDKit.discoverConfiguration("https://id.dev.pace.cloud/auth/realms/pace") {
                authorization_endpoint.visibility = View.VISIBLE

                when (it) {
                    is Success -> {
                        token_endpoint.visibility = View.VISIBLE
                        end_session_endpoint.visibility = View.VISIBLE
                        registration_endpoint.visibility = View.VISIBLE

                        authorization_endpoint.text = "Authorization endpoint: ${it.result.authorizationEndpoint}"
                        token_endpoint.text = "Token endpoint: ${it.result.tokenEndpoint}"
                        end_session_endpoint.text = "End session endpoint: ${it.result.endSessionEndpoint}"
                        registration_endpoint.text = "Registration endpoint: ${it.result.registrationEndpoint}"
                    }
                    is Failure -> {
                        token_endpoint.visibility = View.GONE
                        end_session_endpoint.visibility = View.GONE
                        registration_endpoint.visibility = View.GONE

                        authorization_endpoint.text = it.throwable.message
                    }
                }
            }
        }

        end_session.setOnClickListener {
            lifecycleScope.launch {
                IDKit.endSession(this@MainActivity) {
                    when (it) {
                        is Success -> info_label.text = "Unauthorized"
                        is Failure -> info_label.text = it.throwable.message
                    }
                }
            }
        }

        AppKit.requestCofuGasStations { completion ->
            (completion as? Success)?.result?.count { it.connectedFuelingStatus == ConnectedFuelingStatus.ONLINE }?.let {
                Toast.makeText(this, "$it CoFu gas stations online", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startLocationListener() {
        POIKit.startLocationListener().location.observe(this) {
            val lastLocation = lastLocation
            if (lastLocation == null || lastLocation.distanceTo(it) > APP_DISTANCE_THRESHOLD) {
                AppKit.requestLocalApps { completion ->
                    if (completion is Success) {
                        val apps = completion.result
                        // Only refresh App Drawers if old and new app lists are not equal
                        if (currentApps.size != apps.size || !currentApps.containsAll(apps)) {
                            currentApps = apps
                            AppKit.openApps(this, completion.result, root_layout, bottomMargin = 100f, callback = defaultAppCallback)
                        }
                    }
                }

                this.lastLocation = it
            }
        }
    }

    private fun showUserEmail(completion: Completion<String?>) {
        when (completion) {
            is Success -> completion.result?.let { token ->
                IDKit.userInfo(token) { response ->
                    (response as? Success)?.result?.let { info_label.text = "User email: ${it.email}" }
                }
            }
            is Failure -> info_label.text = "Refresh error: ${completion.throwable.message}"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.credentials) {
            startActivity(Intent(this, CredentialsActivity::class.java))
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val APP_DISTANCE_THRESHOLD = 15
    }
}
