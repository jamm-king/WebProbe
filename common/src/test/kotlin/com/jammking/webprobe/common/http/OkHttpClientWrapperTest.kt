package com.jammking.webprobe.common.http

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OkHttpClientWrapperTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var client: OkHttpClientWrapper

    @BeforeEach
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()
        client = OkHttpClientWrapper()
    }

    @AfterEach
    fun shutdown() {
        mockServer.shutdown()
    }

    @Test
    fun `should return response body and status code for successful GET`() {
        // given
        val expectedBody = "hello world"
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(expectedBody))

        val url = mockServer.url("/test").toString()

        // when
        val response = client.get(url)

        // then
        assertEquals(200, response.statusCode)
        assertEquals(expectedBody, response.body)
        assertNotNull(response.headers)
    }

    @Test
    fun `should return error status code for 404 response`() {
        // given
        mockServer.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

        val url = mockServer.url("/not-found").toString()

        // when
        val response = client.get(url)

        // then
        assertEquals(404, response.statusCode)
        assertEquals("Not Found", response.body)
    }
}