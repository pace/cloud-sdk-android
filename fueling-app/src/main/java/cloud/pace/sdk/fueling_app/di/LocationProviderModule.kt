package cloud.pace.sdk.fueling_app.di

import cloud.pace.sdk.poikit.POIKit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationProviderModule {

    @Singleton
    @Provides
    fun provideLocationProvider() = POIKit.startLocationListener()
}
