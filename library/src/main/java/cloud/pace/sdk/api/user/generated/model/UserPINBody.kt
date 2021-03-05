package cloud.pace.sdk.api.user.generated.model

import java.util.*

class UserPINBody {

    var id: String = UUID.randomUUID().toString()
    var type: String = "pin"
    var attributes: Attributes? = null

    class Attributes {

        var pin: String? = null
        var otp: String? = null
    }
}
