package car.pace.cofu.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import cloud.pace.sdk.idkit.IDKit
import dagger.hilt.android.AndroidEntryPoint

/**
 * Inherit from [AppCompatActivity], which is a [FragmentActivity], since the [BiometricPrompt] only supports this type of activity.
 * Use [AppCompatActivity] instead of [FragmentActivity] because [IDKit.authorize] requires an [AppCompatActivity].
 */
@AndroidEntryPoint
class MainActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This is needed to draw the content edge-to-edge (behind system bars)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AppContent()
        }
    }
}
