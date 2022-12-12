package cloud.pace.sdk.idkit

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl.Companion.getSecureDataPreferenceKey
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl.Companion.getTotpSecretPreferenceKey
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.appkit.persistence.UserRelatedConstants
import cloud.pace.sdk.idkit.authorization.SessionHolder
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

class SharedPreferencesMigrationTest : KoinTest {

    private val accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val sharedPreferencesModel: SharedPreferencesModel by inject()
    private val sessionHolder = mockk<SessionHolder>(relaxed = true)

    @Before
    fun setup() {
        every { sessionHolder.isAuthorizationValid() } returns true
        every { sessionHolder.cachedToken() } returns accessToken

        val module = module {
            single {
                context
            }

            single {
                sessionHolder
            }

            single<SharedPreferencesModel> {
                SharedPreferencesImpl(get(), get())
            }
        }

        startKoin {
            modules(module)
        }

        SharedPreferencesImpl.removeUserPreferences(context, accessToken)
        context.deleteSharedPreferences("${context.packageName}_preferences")
    }

    @Test
    fun testMigrateScopedUserValues() {
        val expected = mapOf(
            getTotpSecretPreferenceKey(UserRelatedConstants.PERIOD.value, null, UserRelatedConstants.PAYMENT_AUTHORIZE.value) to 42,
            getTotpSecretPreferenceKey(UserRelatedConstants.SECRET.value, null, UserRelatedConstants.PAYMENT_AUTHORIZE.value) to "foo",
            getTotpSecretPreferenceKey(UserRelatedConstants.DIGITS.value, null, UserRelatedConstants.PAYMENT_AUTHORIZE.value) to 5,
            getTotpSecretPreferenceKey(UserRelatedConstants.ALGORITHM.value, null, UserRelatedConstants.PAYMENT_AUTHORIZE.value) to "bar",
            getSecureDataPreferenceKey("foo.bar", "hello") to "world"
        )

        val keys = expected.keys.toList()
        val values = expected.values.toList()

        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putInt(keys[0], values[0] as Int)
            putString(keys[1], values[1] as String)
            putInt(keys[2], values[2] as Int)
            putString(keys[3], values[3] as String)
            putString(keys[4], values[4] as String)
        }

        sharedPreferencesModel.migrateUserValuesToUserId()

        assertEquals(expected, SharedPreferencesImpl.getUserPreferences(context, accessToken)?.all)
        assertTrue(keys.none { PreferenceManager.getDefaultSharedPreferences(context).contains(it) })
    }
}
