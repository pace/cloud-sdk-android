package cloud.pace.sdk.utils

import cloud.pace.sdk.poikit.utils.ApiException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class CallBackKt<T> : Callback<T> {

    var onResponse: ((Response<T>) -> Unit)? = null
    var onFailure: ((t: Throwable?) -> Unit)? = null

    override fun onFailure(call: Call<T>, t: Throwable) {
        Timber.e(t, "Request failed: ${call.request().url()}")
        onFailure?.invoke(t)
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (!response.isSuccessful) {
            Timber.e(ApiException(response.code(), response.message()), "Request unsuccessful: ${call.request().url()}")
        }
        onResponse?.invoke(response)
    }
}

fun <T> Call<T>.enqueue(callback: CallBackKt<T>.() -> Unit) {
    val callBackKt = CallBackKt<T>()
    callback.invoke(callBackKt)
    this.enqueue(callBackKt)
}

fun <T> Call<T>.handleCallback(completion: (Completion<T>) -> Unit) {
    enqueue {
        onResponse = {
            val body = it.body()
            if (it.isSuccessful && body != null) {
                completion(Success(body))
            } else {
                completion(Failure(ApiException(it.code(), it.message())))
            }
        }

        onFailure = {
            completion(Failure(it ?: Exception("Unknown exception")))
        }
    }
}

@JvmName("handleCallbackResult")
fun <T> Call<T>.handleCallback(completion: (Result<T>) -> Unit) {
    enqueue {
        onResponse = {
            val body = it.body()
            if (it.isSuccessful && body != null) {
                completion(Result.success(body))
            } else {
                completion(Result.failure(ApiException(it.code(), it.message())))
            }
        }

        onFailure = {
            completion(Result.failure(it ?: Exception("Unknown exception")))
        }
    }
}