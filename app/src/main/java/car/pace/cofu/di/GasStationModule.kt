package car.pace.cofu.di

import car.pace.cofu.data.cache.GasStationCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GasStationModule {

    @Singleton
    @Provides
    fun provideGasStationCache(): GasStationCache {
        return GasStationCache()
    }
}
