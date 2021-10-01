//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated

import cloud.pace.sdk.appkit.communication.generated.model.request.AppRedirectRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.ApplePayAvailabilityCheckRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.ApplePayRequestRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.DisableRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.GetAccessTokenRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.GetConfigRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.GetSecureDataRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.GetTOTPRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.ImageDataRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.LogEventRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.OpenURLInNewTabRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.Request
import cloud.pace.sdk.appkit.communication.generated.model.request.SetSecureDataRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.SetTOTPRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.SetUserPropertyRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.VerifyLocationRequest
import cloud.pace.sdk.appkit.communication.generated.model.response.Message
import cloud.pace.sdk.appkit.communication.generated.model.response.Response
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import kotlin.String
import kotlin.Unit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The middleware between the PACE Cloud SDK and the PWA that routes the message to the correct
 * handler and serialized/deserialized the request and response JSON correctly.
 */
public data class CommunicationManager(
  /**
   * Register the [Communication] listener that invokes the correct message handler when a new PWA
   * message arrives.
   */
  public val listener: Communication,
  /**
   * Called when the response JSON string should be sent to the PWA.
   */
  public val onResponse: (String) -> Unit
) {
  private val gson: Gson = Gson()

  /**
   * Call this method when the PWA sends a new JSON message. The [CommunicationManager]
   * automatically invokes the correct [Communication] handler.
   *
   * @param message The JSON message string
   */
  public fun handleMessage(message: String): Unit {
    val request = gson.fromJson<Any>(message)
    when (request?.uri) {
      "/introspect" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val result = listener.introspect(timeout)
          withContext(Dispatchers.Main) {
            respond(Response(request.id, result.status, request.header, result.body))
          }
        }
      }
      "/close" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val result = listener.close(timeout)
          withContext(Dispatchers.Main) {
            respond(Response(request.id, result.status, request.header, result.body))
          }
        }
      }
      "/logout" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val result = listener.logout(timeout)
          withContext(Dispatchers.Main) {
            respond(Response(request.id, result.status, request.header, result.body))
          }
        }
      }
      "/getBiometricStatus" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val result = listener.getBiometricStatus(timeout)
          withContext(Dispatchers.Main) {
            respond(Response(request.id, result.status, request.header, result.body))
          }
        }
      }
      "/setTOTP" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val setTOTPRequest = gson.fromJson<SetTOTPRequest>(message)
          val body = setTOTPRequest?.body
          if (body == null) {
            withContext(Dispatchers.Main) {
              respond(Response(
                 request.id, HttpURLConnection.HTTP_BAD_REQUEST, request.header,
                 Message("Could not deserialize the JSON request message")
              ))
            }
          } else {
            val result = listener.setTOTP(timeout, body)
            withContext(Dispatchers.Main) {
              respond(Response(request.id, result.status, request.header, result.body))
            }
          }
        }
      }
      "/getTOTP" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val getTOTPRequest = gson.fromJson<GetTOTPRequest>(message)
          val body = getTOTPRequest?.body
          if (body == null) {
            withContext(Dispatchers.Main) {
              respond(Response(
                 request.id, HttpURLConnection.HTTP_BAD_REQUEST, request.header,
                 Message("Could not deserialize the JSON request message")
              ))
            }
          } else {
            val result = listener.getTOTP(timeout, body)
            withContext(Dispatchers.Main) {
              respond(Response(request.id, result.status, request.header, result.body))
            }
          }
        }
      }
      "/setSecureData" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val setSecureDataRequest = gson.fromJson<SetSecureDataRequest>(message)
          val body = setSecureDataRequest?.body
          if (body == null) {
            withContext(Dispatchers.Main) {
              respond(Response(
                 request.id, HttpURLConnection.HTTP_BAD_REQUEST, request.header,
                 Message("Could not deserialize the JSON request message")
              ))
            }
          } else {
            val result = listener.setSecureData(timeout, body)
            withContext(Dispatchers.Main) {
              respond(Response(request.id, result.status, request.header, result.body))
            }
          }
        }
      }
      "/getSecureData" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val getSecureDataRequest = gson.fromJson<GetSecureDataRequest>(message)
          val body = getSecureDataRequest?.body
          if (body == null) {
            withContext(Dispatchers.Main) {
              respond(Response(
                 request.id, HttpURLConnection.HTTP_BAD_REQUEST, request.header,
                 Message("Could not deserialize the JSON request message")
              ))
            }
          } else {
            val result = listener.getSecureData(timeout, body)
            withContext(Dispatchers.Main) {
              respond(Response(request.id, result.status, request.header, result.body))
            }
          }
        }
      }
      "/disable" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val disableRequest = gson.fromJson<DisableRequest>(message)
          val body = disableRequest?.body
          if (body == null) {
            withContext(Dispatchers.Main) {
              respond(Response(
                 request.id, HttpURLConnection.HTTP_BAD_REQUEST, request.header,
                 Message("Could not deserialize the JSON request message")
              ))
            }
          } else {
            val result = listener.disable(timeout, body)
            withContext(Dispatchers.Main) {
              respond(Response(request.id, result.status, request.header, result.body))
            }
          }
        }
      }
      "/openURLInNewTab" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val openURLInNewTabRequest = gson.fromJson<OpenURLInNewTabRequest>(message)
          val body = openURLInNewTabRequest?.body
          if (body == null) {
            withContext(Dispatchers.Main) {
              respond(Response(
                 request.id, HttpURLConnection.HTTP_BAD_REQUEST, request.header,
                 Message("Could not deserialize the JSON request message")
              ))
            }
          } else {
            val result = listener.openURLInNewTab(timeout, body)
            withContext(Dispatchers.Main) {
              respond(Response(request.id, result.status, request.header, result.body))
            }
          }
        }
      }
      "/verifyLocation" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val verifyLocationRequest = gson.fromJson<VerifyLocationRequest>(message)
          val body = verifyLocationRequest?.body
          if (body == null) {
            withContext(Dispatchers.Main) {
              respond(Response(
                 request.id, HttpURLConnection.HTTP_BAD_REQUEST, request.header,
                 Message("Could not deserialize the JSON request message")
              ))
            }
          } else {
            val result = listener.verifyLocation(timeout, body)
            withContext(Dispatchers.Main) {
              respond(Response(request.id, result.status, request.header, result.body))
            }
          }
        }
      }
      "/getAccessToken" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val getAccessTokenRequest = gson.fromJson<GetAccessTokenRequest>(message)
          val body = getAccessTokenRequest?.body
          if (body == null) {
            withContext(Dispatchers.Main) {
              respond(Response(
                 request.id, HttpURLConnection.HTTP_BAD_REQUEST, request.header,
                 Message("Could not deserialize the JSON request message")
              ))
            }
          } else {
            val result = listener.getAccessToken(timeout, body)
            withContext(Dispatchers.Main) {
              respond(Response(request.id, result.status, request.header, result.body))
            }
          }
        }
      }
      "/imageData" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val imageDataRequest = gson.fromJson<ImageDataRequest>(message)
          val body = imageDataRequest?.body
          if (body == null) {
            withContext(Dispatchers.Main) {
              respond(Response(
                 request.id, HttpURLConnection.HTTP_BAD_REQUEST, request.header,
                 Message("Could not deserialize the JSON request message")
              ))
            }
          } else {
            val result = listener.imageData(timeout, body)
            withContext(Dispatchers.Main) {
              respond(Response(request.id, result.status, request.header, result.body))
            }
          }
        }
      }
      "/applePayAvailabilityCheck" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val applePayAvailabilityCheckRequest =
              gson.fromJson<ApplePayAvailabilityCheckRequest>(message)
          val body = applePayAvailabilityCheckRequest?.body
          if (body == null) {
            withContext(Dispatchers.Main) {
              respond(Response(
                 request.id, HttpURLConnection.HTTP_BAD_REQUEST, request.header,
                 Message("Could not deserialize the JSON request message")
              ))
            }
          } else {
            val result = listener.applePayAvailabilityCheck(timeout, body)
            withContext(Dispatchers.Main) {
              respond(Response(request.id, result.status, request.header, result.body))
            }
          }
        }
      }
      "/applePayRequest" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val applePayRequestRequest = gson.fromJson<ApplePayRequestRequest>(message)
          val body = applePayRequestRequest?.body
          if (body == null) {
            withContext(Dispatchers.Main) {
              respond(Response(
                 request.id, HttpURLConnection.HTTP_BAD_REQUEST, request.header,
                 Message("Could not deserialize the JSON request message")
              ))
            }
          } else {
            val result = listener.applePayRequest(timeout, body)
            withContext(Dispatchers.Main) {
              respond(Response(request.id, result.status, request.header, result.body))
            }
          }
        }
      }
      "/back" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val result = listener.back(timeout)
          withContext(Dispatchers.Main) {
            respond(Response(request.id, result.status, request.header, result.body))
          }
        }
      }
      "/appInterceptableLink" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val result = listener.appInterceptableLink(timeout)
          withContext(Dispatchers.Main) {
            respond(Response(request.id, result.status, request.header, result.body))
          }
        }
      }
      "/setUserProperty" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val setUserPropertyRequest = gson.fromJson<SetUserPropertyRequest>(message)
          val body = setUserPropertyRequest?.body
          if (body == null) {
            withContext(Dispatchers.Main) {
              respond(Response(
                 request.id, HttpURLConnection.HTTP_BAD_REQUEST, request.header,
                 Message("Could not deserialize the JSON request message")
              ))
            }
          } else {
            val result = listener.setUserProperty(timeout, body)
            withContext(Dispatchers.Main) {
              respond(Response(request.id, result.status, request.header, result.body))
            }
          }
        }
      }
      "/logEvent" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val logEventRequest = gson.fromJson<LogEventRequest>(message)
          val body = logEventRequest?.body
          if (body == null) {
            withContext(Dispatchers.Main) {
              respond(Response(
                 request.id, HttpURLConnection.HTTP_BAD_REQUEST, request.header,
                 Message("Could not deserialize the JSON request message")
              ))
            }
          } else {
            val result = listener.logEvent(timeout, body)
            withContext(Dispatchers.Main) {
              respond(Response(request.id, result.status, request.header, result.body))
            }
          }
        }
      }
      "/getConfig" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val getConfigRequest = gson.fromJson<GetConfigRequest>(message)
          val body = getConfigRequest?.body
          if (body == null) {
            withContext(Dispatchers.Main) {
              respond(Response(
                 request.id, HttpURLConnection.HTTP_BAD_REQUEST, request.header,
                 Message("Could not deserialize the JSON request message")
              ))
            }
          } else {
            val result = listener.getConfig(timeout, body)
            withContext(Dispatchers.Main) {
              respond(Response(request.id, result.status, request.header, result.body))
            }
          }
        }
      }
      "/getTraceId" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val result = listener.getTraceId(timeout)
          withContext(Dispatchers.Main) {
            respond(Response(request.id, result.status, request.header, result.body))
          }
        }
      }
      "/getLocation" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val result = listener.getLocation(timeout)
          withContext(Dispatchers.Main) {
            respond(Response(request.id, result.status, request.header, result.body))
          }
        }
      }
      "/appRedirect" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val appRedirectRequest = gson.fromJson<AppRedirectRequest>(message)
          val body = appRedirectRequest?.body
          if (body == null) {
            withContext(Dispatchers.Main) {
              respond(Response(
                 request.id, HttpURLConnection.HTTP_BAD_REQUEST, request.header,
                 Message("Could not deserialize the JSON request message")
              ))
            }
          } else {
            val result = listener.appRedirect(timeout, body)
            withContext(Dispatchers.Main) {
              respond(Response(request.id, result.status, request.header, result.body))
            }
          }
        }
      }
      "/isBiometricAuthEnabled" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val result = listener.isBiometricAuthEnabled(timeout)
          withContext(Dispatchers.Main) {
            respond(Response(request.id, result.status, request.header, result.body))
          }
        }
      }
      "/isSignedIn" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val result = listener.isSignedIn(timeout)
          withContext(Dispatchers.Main) {
            respond(Response(request.id, result.status, request.header, result.body))
          }
        }
      }
      "/isRemoteConfigAvailable" -> {
        CoroutineScope(Dispatchers.Default).launch {
          val timeout = TimeUnit.SECONDS.toMillis((request.header?.get("Keep-Alive") as?
              Double)?.toLong() ?: 5)
          val result = listener.isRemoteConfigAvailable(timeout)
          withContext(Dispatchers.Main) {
            respond(Response(request.id, result.status, request.header, result.body))
          }
        }
      }
      else -> {
        CoroutineScope(Dispatchers.Main).launch {
          respond(Response(request?.id, HttpURLConnection.HTTP_BAD_METHOD, request?.header,
              Message("Could not route the following request to the correct handler: $request")))
        }
      }
    }
  }

  private inline fun <reified T> Gson.fromJson(json: String): Request<T>? = try {
     val type = TypeToken.getParameterized(Request::class.java, T::class.java).type
     fromJson(json, type)
  } catch (e: Exception) {
     null
  }

  private fun respond(message: Response): Unit {
    onResponse(gson.toJson(message))
  }
}
