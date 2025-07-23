package com.jammking.webprobe.crawler.adapter

import com.jammking.webprobe.common.http.HttpClient
import com.jammking.webprobe.common.http.HttpResponse
import com.jammking.webprobe.crawler.adapter.fetcher.DefaultUrlFetcher
import com.jammking.webprobe.crawler.exception.FetchFailedException
import com.jammking.webprobe.crawler.model.CrawledPage
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*


class DefaultUrlFetcherTest {

    private val httpClient: HttpClient = mock()
    private val fetcher = DefaultUrlFetcher(httpClient)

    @Test
    fun `should fetch and parse html page`() = runBlocking {
        // given
        val html = """
            <html>
                <head><title>Mock Title</title></head>
                <body>This is mock content.</body>
            </html>
        """.trimIndent()

        whenever(httpClient.get(eq("https://example.com"), any()))
            .thenReturn(HttpResponse(200, html, emptyMap()))

        // when
        val result: CrawledPage = fetcher.fetch("https://example.com")

        // then
        assertEquals("https://example.com", result.url)
        assertEquals("Mock Title", result.title)
        assertTrue(result.text.contains("This is mock content."))
    }

    @Test
    fun `should throw FetchFailedException when body is null`() = runBlocking {
        whenever(httpClient.get(eq("https://example.com"), any()))
            .thenReturn(HttpResponse(200, null, emptyMap()))

        val exception = assertThrows<FetchFailedException> {
            fetcher.fetch("https://example.com")
        }

        assertEquals("https://example.com", exception.url)
    }

    @Test
    fun `should throw FetchFailedException on IO error`() = runBlocking {
        whenever(httpClient.get(eq("https://example.com"), any()))
            .thenThrow(RuntimeException("network failure"))

        val exception = assertThrows<FetchFailedException> {
            fetcher.fetch("https://example.com")
        }

        assertEquals("https://example.com", exception.url)
    }
}