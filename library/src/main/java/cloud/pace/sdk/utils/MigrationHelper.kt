package cloud.pace.sdk.utils

import android.content.Context
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl.Companion.POIKIT_DATABASE_DELETED
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MigrationHelper(
    private val context: Context,
    private val sharedPreferencesModel: SharedPreferencesModel
) {

    init {
        deletePOIKitDatabase()
    }

    private fun deletePOIKitDatabase() {
        if (sharedPreferencesModel.getBoolean(POIKIT_DATABASE_DELETED, false) != true) {
            CoroutineScope(Dispatchers.IO).launch {
                Timber.i("Start deleting POIKit database")
                context.deleteDatabase(POIKIT_DATABASE_NAME).also {
                    Timber.i("Result of POIKit database deletion: $it")
                }

                sharedPreferencesModel.putBoolean(POIKIT_DATABASE_DELETED, true)
            }
        }
    }

    companion object {
        private const val POIKIT_DATABASE_NAME = "poikit_database"
    }
}
