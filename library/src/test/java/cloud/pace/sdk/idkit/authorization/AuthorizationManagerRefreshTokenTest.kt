package cloud.pace.sdk.idkit.authorization

import cloud.pace.sdk.api.API
import cloud.pace.sdk.idkit.model.OIDConfiguration
import cloud.pace.sdk.idkit.userinfo.UserInfoApiClient
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.TokenResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AuthorizationManagerRefreshTokenTest {

    private val mockAuthService = mockk<AuthorizationService>(relaxed = true)
    private val mockSessionHolder = mockk<SessionHolder>()
    private val mockAuthState = mockk<AuthState>(relaxed = true)

    private lateinit var manager: AuthorizationManager
    private var capturedTokenCallback: AuthorizationService.TokenResponseCallback? = null

    @Before
    fun setup() {
        capturedTokenCallback = null

        manager = AuthorizationManager(
            mockk(relaxed = true),
            mockAuthService,
            mockSessionHolder,
            mockk<UserInfoApiClient>(relaxed = true)
        )

        // Set lateinit fields via reflection to avoid calling setup() which needs Android framework
        val config = mockk<OIDConfiguration>(relaxed = true)
        every { config.clientSecret } returns "test-secret"
        every { config.tokenExchangeConfig } returns null
        setPrivateField(manager, "configuration", config)
        setPrivateField(manager, "clientId", "test-client-id")

        // Mock SessionHolder
        every { mockSessionHolder.isAuthorizationValid() } returns true
        every { mockSessionHolder.cachedToken() } returns "refreshed-token"
        every { mockSessionHolder.persistSession() } just Runs
        @Suppress("UNCHECKED_CAST")
        every { mockSessionHolder.withSession<Any?>(any()) } answers {
            val block = invocation.args[0] as Function1<AuthState?, Any?>
            block.invoke(mockAuthState)
        }

        // Mock API singleton
        mockkObject(API)
        every { API.addAuthorizationHeader(any()) } just Runs

        // Capture the token request callback (AppAuth's SAM interface)
        every {
            mockAuthService.performTokenRequest(any(), any<ClientAuthentication>(), any())
        } answers {
            capturedTokenCallback = thirdArg()
        }
    }

    @After
    fun tearDown() {
        unmockkObject(API)
    }

    @Test
    fun `concurrent refreshToken calls trigger only one token request`() {
        val callCount = 5
        val completionLatch = CountDownLatch(callCount)
        val results = CopyOnWriteArrayList<Completion<String?>>()

        repeat(callCount) {
            Thread {
                manager.refreshToken { result ->
                    results.add(result)
                    completionLatch.countDown()
                }
            }.start()
        }

        // Wait for all threads to have entered refreshToken()
        Thread.sleep(200)

        // Only one performTokenRequest should have been made
        verify(exactly = 1) {
            mockAuthService.performTokenRequest(any(), any<ClientAuthentication>(), any())
        }

        // Simulate successful token response via AppAuth callback
        assertNotNull("Token callback should have been captured", capturedTokenCallback)
        capturedTokenCallback!!.onTokenRequestCompleted(mockk<TokenResponse>(relaxed = true), null)

        // All callers should receive results
        assertTrue("Not all callers received results", completionLatch.await(5, TimeUnit.SECONDS))
        assertEquals(callCount, results.size)
        results.forEach { result ->
            assertTrue("Expected Success but got $result", result is Success)
            assertEquals("refreshed-token", (result as Success).result)
        }
    }

    @Test
    fun `sequential refreshToken calls each trigger a new token request`() {
        val result1 = mutableListOf<Completion<String?>>()
        val result2 = mutableListOf<Completion<String?>>()

        // First call
        manager.refreshToken { result1.add(it) }
        assertNotNull("Callback should have been captured", capturedTokenCallback)
        capturedTokenCallback!!.onTokenRequestCompleted(mockk<TokenResponse>(relaxed = true), null)

        assertEquals(1, result1.size)
        assertTrue(result1[0] is Success)

        // Reset for second call
        capturedTokenCallback = null

        // Second call should trigger a new token request
        manager.refreshToken { result2.add(it) }
        assertNotNull("Second callback should have been captured", capturedTokenCallback)
        verify(exactly = 2) {
            mockAuthService.performTokenRequest(any(), any<ClientAuthentication>(), any())
        }

        capturedTokenCallback!!.onTokenRequestCompleted(mockk<TokenResponse>(relaxed = true), null)
        assertEquals(1, result2.size)
        assertTrue(result2[0] is Success)
    }

    @Test
    fun `failure result fans out to all concurrent callers`() {
        val callCount = 3
        val completionLatch = CountDownLatch(callCount)
        val results = CopyOnWriteArrayList<Completion<String?>>()

        repeat(callCount) {
            Thread {
                manager.refreshToken { result ->
                    results.add(result)
                    completionLatch.countDown()
                }
            }.start()
        }

        Thread.sleep(200)

        verify(exactly = 1) {
            mockAuthService.performTokenRequest(any(), any<ClientAuthentication>(), any())
        }

        // Simulate failure
        assertNotNull("Token callback should have been captured", capturedTokenCallback)
        capturedTokenCallback!!.onTokenRequestCompleted(null, AuthorizationException.GeneralErrors.NETWORK_ERROR)

        assertTrue("Not all callers received results", completionLatch.await(5, TimeUnit.SECONDS))
        assertEquals(callCount, results.size)
        results.forEach { result ->
            assertTrue("Expected Failure but got $result", result is Failure)
        }
    }

    @Test
    fun `refreshToken with invalid session returns failure without network request`() {
        every { mockSessionHolder.isAuthorizationValid() } returns false

        val results = mutableListOf<Completion<String?>>()
        manager.refreshToken { results.add(it) }

        assertEquals(1, results.size)
        assertTrue(results[0] is Failure)
        verify(exactly = 0) {
            mockAuthService.performTokenRequest(any(), any<ClientAuthentication>(), any())
        }
    }

    @Test
    fun `invalid session failure fans out to all concurrent callers`() {
        every { mockSessionHolder.isAuthorizationValid() } returns false

        val callCount = 3
        val results = CopyOnWriteArrayList<Completion<String?>>()
        val completionLatch = CountDownLatch(callCount)

        repeat(callCount) {
            Thread {
                manager.refreshToken { result ->
                    results.add(result)
                    completionLatch.countDown()
                }
            }.start()
        }

        assertTrue("Not all callers received results", completionLatch.await(5, TimeUnit.SECONDS))
        assertEquals(callCount, results.size)
        results.forEach { result ->
            assertTrue("Expected Failure but got $result", result is Failure)
        }
        verify(exactly = 0) {
            mockAuthService.performTokenRequest(any(), any<ClientAuthentication>(), any())
        }
    }

    private fun setPrivateField(target: Any, fieldName: String, value: Any) {
        val field = target::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(target, value)
    }
}
