package cloud.pace.sdk.utils

import org.koin.core.KoinComponent

interface IDKitKoinComponent : KoinComponent {

    override fun getKoin() = KoinConfig.idKitKoinApp.koin
}

interface AppKitKoinComponent : KoinComponent {

    override fun getKoin() = KoinConfig.appKitKoinApp.koin
}

interface POIKitKoinComponent : KoinComponent {

    override fun getKoin() = KoinConfig.poiKitKoinApp.koin
}
