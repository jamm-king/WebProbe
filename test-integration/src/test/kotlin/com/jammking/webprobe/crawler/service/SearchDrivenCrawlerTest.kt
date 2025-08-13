package com.jammking.webprobe.crawler.service

import com.jammking.webprobe.CrawlerDataTestApplication
import com.jammking.webprobe.crawler.model.SearchEngine
import com.jammking.webprobe.crawler.model.SearchRequest
import com.jammking.webprobe.data.entity.CrawledPage
import com.jammking.webprobe.data.service.CrawledPageStorage
import com.jammking.webprobe.data.service.UserSeenStorage
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Tag("integration")
@ActiveProfiles("integration")
@SpringBootTest(classes = [CrawlerDataTestApplication::class])
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