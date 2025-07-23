package com.jammking.webprobe.crawler.adapter

import com.jammking.webprobe.crawler.CrawlerTestApplication
import com.jammking.webprobe.crawler.adapter.searcher.TistorySearcher
import com.jammking.webprobe.crawler.model.SearchEngine
import com.jammking.webprobe.crawler.model.SearchRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes=[CrawlerTestApplication::class])
class TistorySearcherTest {

    @Autowired
    lateinit var tistorySearcher: TistorySearcher

    private val log = LoggerFactory.getLogger(this::class.java)

    @Test
    fun `should return blog post urls from Tistory search`() = runBlocking {
        val request = SearchRequest(
            keyword = "포항 식당 리뷰",
            engines = listOf(SearchEngine.TISTORY),
            maxResults = 5
        )

        val result = tistorySearcher.search(request)

        assertTrue(result.isNotEmpty())
        result.forEach { log.info(it) }
    }
}