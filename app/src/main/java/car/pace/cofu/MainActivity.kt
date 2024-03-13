package car.pace.cofu

import android.os.Bundle
import android.view.Window
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import car.pace.cofu.features.analytics.Analytics
import car.pace.cofu.features.analytics.AppOpened
import car.pace.cofu.ui.app.AppContent
import cloud.pace.sdk.idkit.IDKit
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Inherit from [AppCompatActivity], which is a [FragmentActivity], since the [BiometricPrompt] only supports this type of activity.
 * Use [AppCompatActivity] instead of [FragmentActivity] because [IDKit.authorize] requires an [AppCompatActivity].
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analytics.logEvent(AppOpened)

        // This is needed to draw the content edge-to-edge (behind system bars)
        enableEdgeToEdge()

        // This is needed to prevent white status/navigation bar items when dark mode is used, as they would then not be visible on our white background
        setLightBars(window)

        setContent {
            AppContent()
        }
    }

    private fun setLightBars(window: Window) {
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = true
    }
}
