package com.jammking.webprobe.crawler.service

import com.jammking.webprobe.crawler.CrawlerTestApplication
import com.jammking.webprobe.crawler.model.SearchEngine
import com.jammking.webprobe.crawler.model.SearchRequest
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [CrawlerTestApplication::class])
class SearchDrivenCrawlerTest {

    @Autowired
    lateinit var crawler: SearchDrivenCrawler

    private val log = LoggerFactory.getLogger(this::class.java)

    @Test
    fun `should crawl pages using Tistory search engine`() = runBlocking {
        // given
        val request = SearchRequest(
            keyword = "포항 식당 리뷰",
            engines = listOf(SearchEngine.TISTORY),
            maxResults = 3
        )

        // when
        val result = crawler.crawl(request)

        // then
        log.debug("Pages fetched: ${result.pages.size}")
        result.pages.forEach { log.debug("Fetched page: ${it.url}") }

        assertThat(result.pages).isNotEmpty
        result.pages.forEach {
            assertThat(it.url).isNotBlank()
            assertThat(it.title).isNotBlank()
            assertThat(it.html).isNotBlank()
            assertThat(it.text).isNotBlank()
        }

        log.debug("Stats: {}", result.stats)
        log.debug("Errors: {}", result.errors)
    }
}