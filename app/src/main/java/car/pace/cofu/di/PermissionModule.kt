package car.pace.cofu.di

import android.content.Context
import car.pace.cofu.data.PermissionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PermissionModule {

    @Singleton
    @Provides
    fun providePermissionRepository(@ApplicationContext context: Context): PermissionRepository {
        return PermissionRepository(context)
    }
}
