package cloud.pace.sdk.app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.idkit.FailedRetrievingSessionWhileAuthorizing
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.OIDConfiguration
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.utils.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var appUrl: String? = null
    private var radioButtonId = R.id.radio_activity_result_api
    private var lastLocation: Location? = null
    private var authorizationRequested: Boolean = false
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            handleIntent(it.data)
        }
    }
    private val defaultAppCallback = object : AppCallbackImpl() {
        override fun onOpen(app: App?) {
            appUrl = app?.url
            Toast.makeText(this@MainActivity, "POI ID = ${app?.poiId}", Toast.LENGTH_SHORT).show()
        }

        override fun onTokenInvalid(onResult: (String) -> Unit) {
            IDKit.refreshToken { response ->
                (response as? Success)?.result?.let { token -> onResult(token) } ?: run {
                    radioButtonId = R.id.radio_pending_intents
                    authorize(appUrl)
                }
            }
        }

        override fun onCustomSchemeError(context: Context?, scheme: String) {
            context ?: return
            AlertDialog.Builder(context)
                .setTitle("Payment method not available")
                .setMessage("Sorry, this payment method is not supported by this app.")
                .setNeutralButton("Close") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        IDKit.setup(
            this, OIDConfiguration(
                authorizationEndpoint = "YOUR_AUTHORIZATION_ENDPOINT",    // TODO: Replace with your authorization endpoint
                endSessionEndpoint = "YOUR_END_SESSION_ENDPOINT",    // TODO: Replace with your end session endpoint
                tokenEndpoint = "YOUR_TOKEN_ENDPOINT",    // TODO: Replace with your token endpoint
                clientId = "YOUR_CLIENT_ID",    // TODO: Replace with your client ID
                redirectUri = "YOUR_REDIRECT_URI",    // TODO: Replace with your redirect URI (URI scheme should match `appAuthRedirectScheme` in app's build.gradle file)
            )
        )

        PACECloudSDK.setup(
            this, Configuration(
                clientAppName = "PACECloudSDKExample",
                clientAppVersion = BuildConfig.VERSION_NAME,
                clientAppBuild = BuildConfig.VERSION_CODE.toString(),
                apiKey = "YOUR_API_KEY",
                authenticationMode = AuthenticationMode.NATIVE,
                environment = Environment.DEVELOPMENT
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
            if (!authorizationRequested) {
                authorizationRequested = true
                if (IDKit.isAuthorizationValid()) {
                    AppKit.openPaymentApp(this, autoClose = false, callback = defaultAppCallback)
                    authorizationRequested = false
                } else {
                    when (radioButtonId) {
                        R.id.radio_activity_result_api -> startForResult.launch(IDKit.authorize())
                        R.id.radio_pending_intents -> IDKit.authorize(MainActivity::class.java, MainActivity::class.java)
                    }
                }
            }
        }

        fueling_app.setOnClickListener {
            if (!authorizationRequested) {
                authorizationRequested = true
                if (IDKit.isAuthorizationValid()) {
                    AppKit.openFuelingApp(this, autoClose = false, callback = defaultAppCallback)
                    authorizationRequested = false
                } else {
                    when (radioButtonId) {
                        R.id.radio_activity_result_api -> startForResult.launch(IDKit.authorize())
                        R.id.radio_pending_intents -> IDKit.authorize(MainActivity::class.java, MainActivity::class.java)
                    }
                }
            }
        }

        user_info.setOnClickListener {
            if (IDKit.isAuthorizationValid()) {
                IDKit.refreshToken { completion ->
                    when (completion) {
                        is Success -> completion.result?.let {
                            IDKit.userInfo(accessToken = it) {
                                if (it is Success) {
                                    info_label.text = "User email: ${it.result.email}"
                                }
                            }
                        }
                        is Failure -> info_label.text = "Refresh error: ${completion.throwable.message}"
                    }
                }
            } else {
                authorize(null)
            }
        }

        discover_configuration.setOnClickListener {
            // TODO: Replace with your issuerUri
            IDKit.discoverConfiguration("YOUR_ISSUER_URI") {
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
            when (radioButtonId) {
                R.id.radio_activity_result_api -> {
                    val intent = IDKit.endSession()
                    if (intent != null) {
                        startForResult.launch(intent)
                    } else {
                        info_label.text = "Session could not be ended"
                    }
                }
                R.id.radio_pending_intents -> {
                    val success = IDKit.endSession(MainActivity::class.java, MainActivity::class.java)
                    if (!success) {
                        info_label.text = "Session could not be ended"
                    }
                }
            }
        }

        val appListAdapter = AppListAdapter {
            AppKit.openAppActivity(this, it, autoClose = false, callback = defaultAppCallback)
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
    }

    override fun onStart() {
        super.onStart()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            radioButtonId = view.id
        }
    }

    private fun authorize(url: String?) {
        if (authorizationRequested) return
        authorizationRequested = true

        appUrl = url

        if (IDKit.isAuthorizationValid()) {
            openApp()
        } else {
            when (radioButtonId) {
                R.id.radio_activity_result_api -> startForResult.launch(IDKit.authorize())
                R.id.radio_pending_intents -> IDKit.authorize(MainActivity::class.java, MainActivity::class.java)
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null) {
            when {
                IDKit.containsAuthorizationResponse(intent) -> {
                    IDKit.handleAuthorizationResponse(intent) {
                        when (it) {
                            is Success -> openApp()
                            is Failure -> {
                                authorizationRequested = false

                                if (it.throwable != FailedRetrievingSessionWhileAuthorizing) {
                                    info_label.text = "Unauthorized"
                                }
                            }
                        }
                    }
                }
                IDKit.containsEndSessionResponse(intent) -> {
                    IDKit.handleEndSessionResponse(intent) {
                        info_label.text = if ((it as? Success)?.result == Unit) "Unauthorized" else "Session could not be ended"
                    }
                }
            }
        }
    }

    private fun openApp() {
        authorizationRequested = false
        val url = appUrl ?: return

        info_label.text = "Open app"
        AppKit.openAppActivity(context = this, url = url, autoClose = false, callback = object : AppCallbackImpl() {
            override fun onTokenInvalid(onResult: (String) -> Unit) {
                IDKit.refreshToken { completion ->
                    when (completion) {
                        is Success -> completion.result?.let { onResult(it) }
                        is Failure -> info_label.text = "Refresh error: ${completion.throwable.message}"
                    }
                }
            }
        })
    }

    private fun startLocationListener() {
        POIKit.startLocationListener().location.observe(this) {
            val lastLocation = lastLocation
            if (lastLocation == null || lastLocation.distanceTo(it) > APP_DISTANCE_THRESHOLD) {
                AppKit.requestLocalApps { completion ->
                    if (completion is Success) {
                        AppKit.openApps(this, completion.result, root_layout, bottomMargin = 100f, callback = defaultAppCallback)
                    }
                }

                this.lastLocation = it
            }
        }
    }

    companion object {
        private const val APP_DISTANCE_THRESHOLD = 15
    }
}
