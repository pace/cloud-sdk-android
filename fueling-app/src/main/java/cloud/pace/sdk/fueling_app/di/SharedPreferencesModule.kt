package cloud.pace.sdk.fueling_app.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
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
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    fun provideSharedPreferencesImpl(sharedPreferences: SharedPreferences): SharedPreferencesModel {
        return SharedPreferencesImpl(sharedPreferences)
    }
}
