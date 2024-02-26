package car.pace.cofu.di

import android.content.Context
import car.pace.cofu.util.Constants.LOCATION_UPDATE_DISTANCE_METERS
import car.pace.cofu.util.Constants.LOCATION_UPDATE_INTERVAL_MILLIS
import cloud.pace.sdk.utils.LocationProvider
import cloud.pace.sdk.utils.LocationProviderImpl
import cloud.pace.sdk.utils.SystemManager
import cloud.pace.sdk.utils.SystemManagerImpl
import com.google.android.gms.location.LocationRequest
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
    fun provideLocationRequest(): LocationRequest {
        return LocationRequest.Builder(LOCATION_UPDATE_INTERVAL_MILLIS)
            .setMinUpdateDistanceMeters(LOCATION_UPDATE_DISTANCE_METERS)
            .build()
    }

    @Singleton
    @Provides
    fun provideLocationProvider(@ApplicationContext context: Context, systemManager: SystemManager): LocationProvider {
        return LocationProviderImpl(context, systemManager)
    }
}
