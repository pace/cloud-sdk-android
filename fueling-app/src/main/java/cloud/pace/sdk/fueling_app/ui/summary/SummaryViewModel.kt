package cloud.pace.sdk.fueling_app.ui.summary

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import cloud.pace.sdk.fueling_app.data.repository.Repository
import cloud.pace.sdk.fueling_app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: Repository
) : ViewModel() {

    val transactionId by lazy { MutableLiveData<String>() }
    val receipt = transactionId.switchMap {
        liveData {
            emit(Result.Loading)

            try {
                emit(Result.Success(repository.getReceipt(context, it)))
            } catch (e: Exception) {
                emit(Result.Error(e))
            }
        }
    }
}
