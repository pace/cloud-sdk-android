package car.pace.cofu.ui.wallet

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import car.pace.cofu.BuildConfig
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.UserRepository
import car.pace.cofu.ui.Route
import car.pace.cofu.util.IntentUtils
import car.pace.cofu.util.LogAndBreadcrumb
import cloud.pace.sdk.appkit.AppKit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    val items = buildList {
        addAll(listOf(Route.PAYMENT_METHODS, Route.TRANSACTIONS, Route.FUEL_TYPE, Route.AUTHORIZATION, Route.DELETE_ACCOUNT))

        if (BuildConfig.HIDE_PRICES) {
            remove(Route.FUEL_TYPE)
        }

        if (!sharedPreferencesRepository.getBoolean(SharedPreferencesRepository.PREF_KEY_TWO_FACTOR_AVAILABLE, true)) {
            remove(Route.AUTHORIZATION)
        }
    }

    suspend fun resetAppData(activity: AppCompatActivity) {
        userRepository.resetAppData(activity)
    }

    fun onDeleteAccount(context: Context) {
        LogAndBreadcrumb.d(LogAndBreadcrumb.WALLET, "Page for account deletion gets displayed")

        val externalAccountLink = BuildConfig.EXTERNAL_OIDC_ACCOUNT_DELETION_URL

        if (externalAccountLink != null) {
            IntentUtils.launchInCustomTabIfAvailable(context, externalAccountLink)
        } else {
            AppKit.openPaceID(context)
        }
    }
}
