package com.raywenderlich.android.petsave.common.data.api.interceptors

import android.os.Build
import com.raywenderlich.android.petsave.common.data.api.ApiConstants.ANIMALS_ENDPOINT
import com.raywenderlich.android.petsave.common.data.api.ApiConstants.AUTH_ENDPOINT
import com.raywenderlich.android.petsave.common.data.preferences.Preferences
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import com.google.common.truth.Truth.*
import com.raywenderlich.android.petsave.common.data.api.ApiParameters

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class AuthenticationInterceptorTest {
    private lateinit var preferences: Preferences
    private lateinit var mockWebServer: MockWebServer
    private lateinit var authenticationInterceptor: AuthenticationInterceptor
    private lateinit var okHttpClient: OkHttpClient

    private val endpointSeparator = "/"
    private val animalsEndpointPath = endpointSeparator + ANIMALS_ENDPOINT
    private val authEndpointPath = endpointSeparator + AUTH_ENDPOINT
    private val validToken = "validToken"
    private val expiredToken = "expiredToken"

    @Before
    fun setup() {
        preferences = mock(Preferences::class.java)
        mockWebServer = MockWebServer()
        mockWebServer.start(8080)

        authenticationInterceptor = AuthenticationInterceptor(preferences)
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authenticationInterceptor)
            .build()
    }
    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun authenticationInterceptor_validToken() {
        // Given
        `when`(preferences.getToken()).thenReturn(validToken)
        `when`(preferences.getTokenExpirationTime()).thenReturn(
            Instant.now().plusSeconds(3600).epochSecond
        )
        mockWebServer.dispatcher = getDispatcherForValidToken()

        // When
        okHttpClient.newCall(
            Request.Builder()
                .url(mockWebServer.url(ANIMALS_ENDPOINT))
                .build()
        )

        // Then
        val request = mockWebServer.takeRequest()

        with(request) {
            assertThat(method).isEqualTo("GET")
            assertThat(path).isEqualTo(animalsEndpointPath)
            assertThat(getHeader(ApiParameters.AUTH_HEADER)).isEqualTo(ApiParameters.AUTH_HEADER + validToken)
        }
    }

    @Test
    fun authenticationInterceptor_invalidToken() {
        // Given
        `when`(preferences.getToken()).thenReturn(expiredToken)
        `when`(preferences.getTokenExpirationTime()).thenReturn(
            Instant.now().minusSeconds(3600).epochSecond
        )
        mockWebServer.dispatcher = getDispatcherForInvalidToken()

        // When
        // Then
    }

    private fun getDispatcherForValidToken() = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return when (request.path) {
                animalsEndpointPath -> { MockResponse().setResponseCode(200) }
                else -> { MockResponse().setResponseCode(404) }
            }
        }
    }

    private fun getDispatcherForInvalidToken() = object: Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return when (request.path) {
                authEndpointPath -> { MockResponse().setResponseCode(200).setBody("validToken.json") }
                animalsEndpointPath -> { MockResponse().setResponseCode(200) }
                else -> { MockResponse().setResponseCode(404) }
            }
        }
    }

}