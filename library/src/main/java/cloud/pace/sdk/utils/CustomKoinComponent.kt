package cloud.pace.sdk.utils

import org.koin.core.KoinComponent

interface IDKitKoinComponent : KoinComponent {

    override fun getKoin() = KoinConfig.idKitKoinApp.koin
}

interface CloudSDKKoinComponent : KoinComponent {

    override fun getKoin() = KoinConfig.cloudSDKKoinApp.koin
}
