package cloud.pace.sdk.utils

import cloud.pace.sdk.PACECloudSDK

object URL {

    val paceID: String
        get() = PACECloudSDK.environment.idUrl

    val payment: String
        get() = PACECloudSDK.environment.payUrl

    val transactions: String
        get() = PACECloudSDK.environment.transactionUrl

    val dashboard: String
        get() = PACECloudSDK.environment.dashboardUrl

    val fueling: String
        get() = PACECloudSDK.environment.fuelingUrl
}
