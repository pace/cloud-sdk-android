package cloud.pace.sdk.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.api.geo.ConnectedFuelingStatus
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.OIDConfiguration
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var lastLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        IDKit.setup(
            this, OIDConfiguration(
                authorizationEndpoint = "https://id.dev.pace.cloud/auth/realms/pace/protocol/openid-connect/auth",
                endSessionEndpoint = "https://id.dev.pace.cloud/auth/realms/pace/protocol/openid-connect/logout",
                tokenEndpoint = "https://id.dev.pace.cloud/auth/realms/pace/protocol/openid-connect/token",
                userInfoEndpoint = "https://id.dev.pace.cloud/auth/realms/pace/protocol/openid-connect/userinfo",
                clientId = "cloud-sdk-example-app",
                redirectUri = "pace://cloud-sdk-example"
            )
        )

        PACECloudSDK.setup(
            this, Configuration(
                clientAppName = "PACECloudSDKExample",
                clientAppVersion = BuildConfig.VERSION_NAME,
                clientAppBuild = BuildConfig.VERSION_CODE.toString(),
                apiKey = "YOUR_API_KEY",
                authenticationMode = AuthenticationMode.NATIVE,
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

        payment_app.setOnClickListener {
            AppKit.openPaymentApp(this)
        }

        fueling_app.setOnClickListener {
            AppKit.openFuelingApp(this)
        }

        transactions_app.setOnClickListener {
            AppKit.openTransactions(this)
        }

        pace_id_app.setOnClickListener {
            AppKit.openPaceID(this)
        }

        user_info.setOnClickListener {
            if (IDKit.isAuthorizationValid()) {
                IDKit.refreshToken {
                    showUserEmail(it)
                }
            } else {
                lifecycleScope.launch(Dispatchers.Main) {
                    IDKit.authorize(this@MainActivity) {
                        showUserEmail(it)
                    }
                }
            }
        }

        discover_configuration.setOnClickListener {
            IDKit.discoverConfiguration("https://id.dev.pace.cloud/auth/realms/pace") {
                when (it) {
                    is Success -> {
                        authorization_endpoint.text = "Authorization endpoint: ${it.result.authorizationEndpoint}"
                        token_endpoint.text = "Token endpoint: ${it.result.tokenEndpoint}"
                        end_session_endpoint.text = "End session endpoint: ${it.result.endSessionEndpoint}"
                        registration_endpoint.text = "Registration endpoint: ${it.result.registrationEndpoint}"
                    }
                    is Failure -> {
                        authorization_endpoint.text = it.throwable.message
                    }
                }
            }
        }

        end_session.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                IDKit.endSession(this@MainActivity) {
                    when (it) {
                        is Success -> info_label.text = "Unauthorized"
                        is Failure -> info_label.text = it.throwable.message
                    }
                }
            }
        }

        val appListAdapter = AppListAdapter {
            AppKit.openAppActivity(this, it, autoClose = false)
        }
        app_list.adapter = appListAdapter
        show_app_list.setOnClickListener { button ->
            button.isEnabled = false
            AppKit.requestLocalApps {
                when (it) {
                    is Success -> {
                        appListAdapter.entries = it.result
                        empty_view.isVisible = it.result.isEmpty()
                    }
                    is Failure -> {
                        appListAdapter.entries = emptyList()
                        empty_view.visibility = View.VISIBLE
                    }
                }
                button.isEnabled = true
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
                        AppKit.openApps(this, completion.result, root_layout, bottomMargin = 100f)
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
