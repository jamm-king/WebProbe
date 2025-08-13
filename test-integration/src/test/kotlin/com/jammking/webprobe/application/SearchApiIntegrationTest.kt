package com.jammking.webprobe.application

import com.jammking.webprobe.WebProbeIntegrationTestApp
import com.jammking.webprobe.crawler.adapter.robots.RobotsTxtEvaluator
import com.jammking.webprobe.crawler.model.SearchEngine
import com.jammking.webprobe.crawler.port.UrlFetcher
import com.jammking.webprobe.crawler.service.resolver.UrlFetcherResolver
import com.jammking.webprobe.data.entity.CrawledPage
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@Tag("integration")
@ActiveProfiles("integration")
@SpringBootTest(
    classes = [WebProbeIntegrationTestApp::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient
class SearchApiIntegrationTest(
    @Autowired private val webTestClient: WebTestClient
) {
    @MockBean
    lateinit var urlFetcherResolver: UrlFetcherResolver
    @MockBean
    lateinit var robotsTxtEvaluator: RobotsTxtEvaluator

    @Test
    fun `POST search returns pages end-to-end`() = runTest {
        whenever(robotsTxtEvaluator.isAllowed(any(), any(), any())).thenReturn(true)

        val searchHtml = """
            <html>
                <a class="link_cont" href="https://site.test/p1">1</a>
                <a class="link_cont" href="https://site.test/p2">2</a>
            </html>
        """.trimIndent()

        val searchPageFetcher = mock<UrlFetcher> {
            onBlocking { fetch(argThat { url -> url.startsWith("https://www.tistory.com/search") }) } doAnswer {
                val url = it.getArgument<String>(0)
                CrawledPage(url, "search", searchHtml, "search")
            }
        }
        val contentFetcher = mock<UrlFetcher> {
            onBlocking { fetch(eq("https://site.test/p1")) } doReturn CrawledPage("https://site.test/p1", "T1", "<html>1</html>", "Text1")
            onBlocking { fetch(eq("https://site.test/p2")) } doReturn CrawledPage("https://site.test/p2", "T2", "<html>2</html>", "Text2")
        }

        whenever(urlFetcherResolver.resolve(any()))
            .thenAnswer { inv ->
                val url = inv.getArgument<String>(0)
                if(url.contains("tistory.com/search")) searchPageFetcher
                else if(url.startsWith("https://site.test/")) contentFetcher
                else throw IllegalArgumentException("unexpected url: $url")
            }

        val body = mapOf(
            "keyword" to "포항 식당 리뷰",
            "engines" to listOf(SearchEngine.TISTORY),
            "maxResults" to 2,
            "userId" to "u1",
            "fresh" to true
        )

        webTestClient.post().uri("/api/search")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.pages.length()").isEqualTo(2)
            .jsonPath("$.stats.successCount").isEqualTo(2)
            .jsonPath("$.errors").isMap
    }
}