package cloud.pace.sdk.utils

import org.koin.core.component.KoinComponent

interface CloudSDKKoinComponent : KoinComponent {

    override fun getKoin() = KoinConfig.cloudSDKKoinApp.koin
}
