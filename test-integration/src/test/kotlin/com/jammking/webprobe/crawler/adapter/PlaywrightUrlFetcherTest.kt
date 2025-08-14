package com.jammking.webprobe.crawler.adapter

import com.jammking.webprobe.CrawlerDataTestApplication
import com.jammking.webprobe.crawler.adapter.fetcher.PlaywrightUrlFetcher
import com.jammking.webprobe.data.entity.CrawledPage
import com.jammking.webprobe.data.service.CrawledPageStorage
import com.jammking.webprobe.data.service.UserSeenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Tag("integration")
@ActiveProfiles("integration")
@SpringBootTest(classes = [CrawlerDataTestApplication::class])
class PlaywrightUrlFetcherTest(
    @Autowired private val fetcher: PlaywrightUrlFetcher,
    @Autowired private val crawledPageStorage: CrawledPageStorage,
    @Autowired private val userSeenStorage: UserSeenStorage
) {

    private lateinit var server: MockWebServer

    @BeforeEach
    fun setup() {
        server = MockWebServer()
        crawledPageStorage.deleteAll()
        userSeenStorage.deleteAll()
    }

    @AfterEach
    fun cleanup() {
        server.shutdown()
        crawledPageStorage.deleteAll()
        userSeenStorage.deleteAll()
    }

    @Test
    fun `should fetch and render html with javascript via Playwright`(): Unit = runBlocking {
        // given
        val html = """
            <!doctype html>
            <html>
                <head>
                    <meta charset="utf-8">
                    <title>Title</title>
                </head>
                <body>
                    <h1 id="greet">Hello WebProbe</h1>
                    <p>This is for text extraction</p>
                    <script>
                        document.title = "rendering check";
                    </script>
                </body>
            </html>
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .addHeader("Content-Type", "text/html; charset=utf-8")
                .setBody(html)
        )
        server.start()
        val url = server.url("/test").toString()

        // when
        val result: CrawledPage = fetcher.fetch(url)

        // then
        assertThat(result.url).isEqualTo(url)
        assertThat(result.title).isEqualTo("rendering check")
        assertThat(result.html).contains("<html").contains("</html>")
        assertThat(result.text).isNotBlank
        assertThat(result.text).contains("Hello WebProbe")
    }
}