package cloud.pace.sdk.idkit.authorization

import cloud.pace.sdk.api.API
import cloud.pace.sdk.idkit.model.OIDConfiguration
import cloud.pace.sdk.idkit.userinfo.UserInfoApiClient
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

class AuthorizationManagerTokenRevocationTest {

    private val mockAuthService = mockk<AuthorizationService>(relaxed = true)
    private val mockSessionHolder = mockk<SessionHolder>()
    private val mockAuthState = mockk<AuthState>(relaxed = true)

    private lateinit var manager: AuthorizationManager
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        manager = AuthorizationManager(
            mockk(relaxed = true),
            mockAuthService,
            mockSessionHolder,
            mockk<UserInfoApiClient>(relaxed = true)
        )

        val config = mockk<OIDConfiguration>(relaxed = true)
        every { config.clientSecret } returns null
        every { config.tokenExchangeConfig } returns null
        every { config.tokenRevocationEndpoint } returns mockWebServer.url("/revoke").toString()
        setPrivateField(manager, "configuration", config)
        setPrivateField(manager, "clientId", "test-client-id")

        every { mockSessionHolder.isAuthorizationValid() } returns true
        every { mockSessionHolder.clearSessionAndPreferences() } just Runs
        every { mockSessionHolder.cachedToken() } returns "access-token"
        @Suppress("UNCHECKED_CAST")
        every { mockSessionHolder.withSession<Any?>(any()) } answers {
            val block = invocation.args[0] as Function1<AuthState?, Any?>
            block.invoke(mockAuthState)
        }

        every { mockAuthState.refreshToken } returns "test-refresh-token"

        mockkObject(API)
        every { API.addAuthorizationHeader(any()) } just Runs
    }

    @After
    fun tearDown() {
        unmockkObject(API)
        mockWebServer.shutdown()
    }

    @Test
    fun `revokeToken sends correct HTTP POST`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        manager.resetSession()

        val request = mockWebServer.takeRequest(5, TimeUnit.SECONDS)
        assertNotNull("Expected HTTP request to revocation endpoint", request)
        assertEquals("POST", request!!.method)

        val body = request.body.readUtf8()
        val params = parseFormBody(body)
        assertEquals("test-client-id", params["client_id"])
        assertEquals("test-refresh-token", params["token"])
        assertEquals("refresh_token", params["token_type_hint"])
    }

    @Test
    fun `revokeToken skips when no refresh token`() {
        every { mockAuthState.refreshToken } returns null

        manager.resetSession()

        val request = mockWebServer.takeRequest(500, TimeUnit.MILLISECONDS)
        assertNull("Expected no HTTP request when refresh token is null", request)
    }

    @Test
    fun `revokeToken skips when no endpoint configured`() {
        val config = mockk<OIDConfiguration>(relaxed = true)
        every { config.clientSecret } returns null
        every { config.tokenExchangeConfig } returns null
        every { config.tokenRevocationEndpoint } returns null
        setPrivateField(manager, "configuration", config)

        manager.resetSession()

        val request = mockWebServer.takeRequest(500, TimeUnit.MILLISECONDS)
        assertNull("Expected no HTTP request when endpoint is null", request)
    }

    @Test
    fun `resetSession clears local state even when revocation fails`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(503))

        manager.resetSession()

        // Wait for async revocation request to complete
        mockWebServer.takeRequest(5, TimeUnit.SECONDS)

        verify { API.addAuthorizationHeader(null) }
        verify { mockSessionHolder.clearSessionAndPreferences() }
    }

    @Test
    fun `resetSession clears local state when no refresh token`() {
        every { mockAuthState.refreshToken } returns null

        manager.resetSession()

        verify { API.addAuthorizationHeader(null) }
        verify { mockSessionHolder.clearSessionAndPreferences() }
    }

    private fun parseFormBody(body: String): Map<String, String> {
        return body.split("&").associate { param ->
            val (key, value) = param.split("=", limit = 2)
            URLDecoder.decode(key, "UTF-8") to URLDecoder.decode(value, "UTF-8")
        }
    }

    private fun setPrivateField(target: Any, fieldName: String, value: Any) {
        val field = target::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(target, value)
    }
}
