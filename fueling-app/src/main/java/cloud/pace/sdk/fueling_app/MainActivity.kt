package cloud.pace.sdk.fueling_app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import cloud.pace.sdk.fueling_app.databinding.ActivityMainBinding
import cloud.pace.sdk.utils.applyInsets
import cloud.pace.sdk.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        binding.root.applyInsets()
    }
}
