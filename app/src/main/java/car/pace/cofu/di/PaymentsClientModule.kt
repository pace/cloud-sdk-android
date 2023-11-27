package car.pace.cofu.di

import android.content.Context
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.utils.Environment
import cloud.pace.sdk.utils.environment
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaymentsClientModule {

    @Singleton
    @Provides
    fun providePaymentsClient(@ApplicationContext context: Context): PaymentsClient {
        val payEnvironment = if (PACECloudSDK.environment == Environment.PRODUCTION) WalletConstants.ENVIRONMENT_PRODUCTION else WalletConstants.ENVIRONMENT_TEST
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(payEnvironment)
            .build()

        return Wallet.getPaymentsClient(context, walletOptions)
    }
}
