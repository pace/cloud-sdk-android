package cloud.pace.sdk.utils

import org.koin.core.KoinComponent

interface CustomKoinComponent : KoinComponent {

    override fun getKoin() = KoinConfig.koinApplication.koin
}
