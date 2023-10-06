package car.pace.cofu.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat

class MainActivity2 : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This is needed to draw the content edge-to-edge (behind system bars)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AppContent()
        }
    }
}
