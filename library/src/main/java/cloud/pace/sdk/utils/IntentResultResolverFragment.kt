package cloud.pace.sdk.utils

import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IntentResultResolverFragment : Fragment() {

    private var completableDeferred: CompletableDeferred<IntentResult>? = null
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val result = if (it.resultCode == Activity.RESULT_OK) Ok(it.data) else Canceled(it.data)
        completableDeferred?.complete(result)
    }

    suspend fun resolveIntentResult(intent: Intent): IntentResult {
        return CompletableDeferred<IntentResult>().run {
            completableDeferred = this
            startForResult.launch(intent)
            await()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        completableDeferred?.cancel()
        completableDeferred = null
    }
}

sealed class IntentResult
data class Ok(val data: Intent?) : IntentResult()
data class Canceled(val data: Intent?) : IntentResult()

suspend fun Activity.getResultFor(intent: Intent): IntentResult {
    return execute(intent)
}

suspend fun Fragment.getResultFor(intent: Intent): IntentResult {
    return execute(intent)
}

private suspend fun Any.execute(intent: Intent): IntentResult {
    // Get fragmentManager depending on the context
    val fragmentManager = when (this) {
        is Fragment -> childFragmentManager
        is AppCompatActivity -> supportFragmentManager
        else -> throw IllegalArgumentException("You must request intent result from an AppCompatActivity or Fragment. It was $this")
    }

    return withContext(Dispatchers.Main) {
        val recycledFragment = fragmentManager.findFragmentByTag(IntentResultResolverFragment::class.java.simpleName)

        if ((recycledFragment as? IntentResultResolverFragment) != null) {
            recycledFragment.resolveIntentResult(intent)
        } else {
            val fragment = IntentResultResolverFragment()
            fragmentManager.commitNow { add(fragment, IntentResultResolverFragment::class.java.simpleName) }
            fragment.resolveIntentResult(intent)
        }
    }
}
