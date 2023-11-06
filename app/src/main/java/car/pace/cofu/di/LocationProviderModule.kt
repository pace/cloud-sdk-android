package car.pace.cofu.di

import android.content.Context
import cloud.pace.sdk.utils.LocationProvider
import cloud.pace.sdk.utils.LocationProviderImpl
import cloud.pace.sdk.utils.SystemManager
import cloud.pace.sdk.utils.SystemManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationProviderModule {

    @Singleton
    @Provides
    fun provideSystemManager(@ApplicationContext context: Context): SystemManager {
        return SystemManagerImpl(context)
    }

    @Singleton
    @Provides
    fun provideLocationProvider(@ApplicationContext context: Context, systemManager: SystemManager): LocationProvider {
        return LocationProviderImpl(context, systemManager)
    }
}
