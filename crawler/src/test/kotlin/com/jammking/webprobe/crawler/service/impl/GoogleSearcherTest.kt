package com.jammking.webprobe.crawler.service.impl

import com.jammking.webprobe.crawler.CrawlerTestApplication
import com.jammking.webprobe.crawler.adapter.searcher.GoogleSearcher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [CrawlerTestApplication::class])
class GoogleSearcherTest {

    @Autowired
    lateinit var googleSearcher: GoogleSearcher

    private val log = LoggerFactory.getLogger(this::class.java)

    @Test
    fun `should return urls for a given keyword`() = runBlocking {
        // given
        val keyword = "포항 분위기 좋은 카페"

        // when
        val urls = googleSearcher.search(keyword)

        // then
        assertTrue(urls.isNotEmpty(), "Search result should not be empty.")
        assertTrue(urls.all { it.startsWith("http")}, "Every url must start with 'http'.")
        log.info("Result URL list:")
        urls.forEach { log.info(it) }
    }
}