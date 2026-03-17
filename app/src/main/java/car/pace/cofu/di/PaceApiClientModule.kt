package car.pace.cofu.di

import car.pace.api.PaceApiClient
import car.pace.api.PaceApiKit
import car.pace.cofu.BuildConfig
import car.pace.cofu.data.interceptor.ApiTokenInterceptor
import cloud.pace.sdk.api.interceptor.AuthenticationInterceptor
import cloud.pace.sdk.utils.Environment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaceApiClientModule {

    private val apiKitEnvironment = when (Environment.values()[BuildConfig.ENVIRONMENT]) {
        Environment.PRODUCTION -> car.pace.api.core.Environment.PRODUCTION
        Environment.SANDBOX -> car.pace.api.core.Environment.SANDBOX
        Environment.DEVELOPMENT -> car.pace.api.core.Environment.DEVELOPMENT
    }

    @Singleton
    @Provides
    fun providePaceApiClient(): PaceApiClient {
        return PaceApiKit.init(apiKitEnvironment, listOf(ApiTokenInterceptor(), AuthenticationInterceptor()))
    }
}
