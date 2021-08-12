package com.raywenderlich.android.petsave.common.data.api.utils

import androidx.test.platform.app.InstrumentationRegistry
import com.raywenderlich.android.logging.Logger
import com.raywenderlich.android.petsave.common.data.api.ApiConstants
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.io.IOException
import java.io.InputStream

class FakeServer {
    private val mockWebServer = MockWebServer()

    private val endpointSeparator = "/"
    private val animalsEndpointPath = endpointSeparator + ApiConstants.ANIMALS_ENDPOINT
    private val notFoundResponse = MockResponse().setResponseCode(404)

    val baseEndpoint
        get() = mockWebServer.url(endpointSeparator)

    fun start() {
        mockWebServer.start(8080)
    }
    fun shutdown() {
        mockWebServer.shutdown()
    }

    fun setHappyPathDispatcher() {
        mockWebServer.dispatcher = object: Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path ?: return notFoundResponse

                return with(path) {
                    when {
                        startsWith(animalsEndpointPath) -> {
                            MockResponse().setResponseCode(200).setBody(getJson("animals.json"))
                        }
                        else -> { notFoundResponse }
                    }
                }
            }

        }
    }
    private fun getJson(path: String): String {
        return try {
            val context = InstrumentationRegistry.getInstrumentation().context
            val jsonStream: InputStream = context.assets.open("networkresponses/$path")
            String(jsonStream.readBytes())
        } catch (exception: IOException) {
            Logger.e(exception, "Error reading network response json asset")
            throw exception
        }
    }
}