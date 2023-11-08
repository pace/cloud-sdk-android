package car.pace.cofu.di

import android.app.Application
import car.pace.cofu.repository.UserDataRepository
import car.pace.cofu.repository.UserDataRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideUserDataRepo(app: Application): UserDataRepository = UserDataRepositoryImpl(app)
}
