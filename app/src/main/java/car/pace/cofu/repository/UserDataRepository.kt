package car.pace.cofu.repository

/**
 * Repository to access data that is locally stored on the device (independant from the PACE-ID account)
 * It includes the check whether the onboarding is completed, if it's not, the currently displayed page
 * and the fuel type selection
 */
interface UserDataRepository {

    /**
     * removes locally stored data (not including information connected to PACE-ID or the login to PACE-ID itself)
     */
    fun clear()

    /**
     * Indicates that the onboarding has been completed.
     */
    var onboardingDone: Boolean

    /**
     * Contains the currently selected fuel type, if there was any selected.
     */
    var fuelType: FuelType?

    /**
     * Contains users email address after successful sign in.
     */
    var email: String?
}