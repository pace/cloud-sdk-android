package car.pace.cofu.modules

import android.app.Application
import car.pace.cofu.core.resources.ResourcesProvider
import car.pace.cofu.core.resources.ResourcesProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class MainModule {

    @Provides
    @Singleton
    fun provideResourcesProvider(app: Application): ResourcesProvider {
        return ResourcesProviderImpl(app)
    }
}