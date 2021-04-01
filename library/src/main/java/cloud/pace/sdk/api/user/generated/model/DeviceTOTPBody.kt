package cloud.pace.sdk.api.user.generated.model

import java.util.*

class DeviceTOTPBody {

    var id: String = UUID.randomUUID().toString()
    var type: String = "deviceTOTP"
    var attributes: Attributes? = null

    class Attributes {

        var pin: String? = null
        var password: String? = null
        var otp: String? = null
    }
}
