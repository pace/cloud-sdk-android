package cloud.pace.sdk.fueling_app.di

import android.content.Context
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object SharedPreferencesModule {

    @Provides
    fun provideSharedPreferencesImpl(@ApplicationContext context: Context): SharedPreferencesModel {
        return SharedPreferencesImpl(context)
    }
}
