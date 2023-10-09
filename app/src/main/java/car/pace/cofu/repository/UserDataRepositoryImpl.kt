package car.pace.cofu.repository

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_FUEL_TYPE
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_ONBOARDING_DONE
import javax.inject.Inject

class UserDataRepositoryImpl @Inject constructor(private val app: Application) :
    UserDataRepository {

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(app)

    override var onboardingDone: Boolean
        get() = sharedPreferences.getBoolean(PREF_KEY_ONBOARDING_DONE, false)
        set(value) {
            sharedPreferences.edit().putBoolean(PREF_KEY_ONBOARDING_DONE, value).apply()
        }

    override var fuelType: FuelType?
        get() = sharedPreferences.getInt(PREF_KEY_FUEL_TYPE, -1)
            .takeIf { it != -1 }
            ?.let { FuelType.values()[it] }
        set(value) {
            if (value == null) {
                sharedPreferences.edit().remove(PREF_KEY_FUEL_TYPE).apply()
            } else {
                sharedPreferences.edit().putInt(PREF_KEY_FUEL_TYPE, value.ordinal).apply()
            }
        }

    override var email: String? = null

    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
