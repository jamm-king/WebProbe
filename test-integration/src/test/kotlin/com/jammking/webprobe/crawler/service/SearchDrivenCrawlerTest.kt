package com.jammking.webprobe.crawler.service

import com.jammking.webprobe.CrawlerDataTestApplication
import com.jammking.webprobe.crawler.model.SearchEngine
import com.jammking.webprobe.crawler.model.SearchRequest
import kotlinx.coroutines.runBlocking
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
    lateinit var crawler: Crawler

    private val log = LoggerFactory.getLogger(this::class.java)

    @Test
    fun `should return blog post successfully`() = runBlocking {
        val keyword = "포항 식당 리뷰"
        val engines = listOf(SearchEngine.TISTORY)
        val maxResults = 10
        val userId = "user-test"
        val fresh = true
        val req = SearchRequest(
            keyword = keyword,
            engines = engines,
            maxResults = maxResults,
            userId = userId,
            fresh = fresh
        )

        val result = crawler.crawl(req)

        log.debug("[stats]")
        log.debug("total : {} success : {}, fail : {}", result.stats.totalUrls, result.stats.successCount, result.stats.failureCount)
        log.debug("[pages]")
        result.pages.forEach { page ->
            log.debug("url : {}", page.url)
        }
        log.debug("[errors]")
        result.errors.forEach { error ->
            log.debug("{} : {}", error.key, error.value)
        }
    }
}