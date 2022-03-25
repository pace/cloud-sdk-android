package cloud.pace.sdk.fueling_app.di

import cloud.pace.sdk.fueling_app.data.api.DataSource
import cloud.pace.sdk.fueling_app.data.api.DataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindDataSource(dataSourceImpl: DataSourceImpl): DataSource
}
