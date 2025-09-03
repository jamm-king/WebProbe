package com.jammking.webprobe.crawler.adapter

import com.jammking.webprobe.crawler.adapter.transformer.TistoryTransformer
import com.jammking.webprobe.data.entity.CrawledPage
import com.microsoft.playwright.Page
import com.microsoft.playwright.junit.UsePlaywright
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.platform.commons.logging.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId

@UsePlaywright
class TistoryTransformerTest {

    private val log = LoggerFactory.getLogger(this::class.java)

    private val transformer = TistoryTransformer()

    @Test
    fun `support tistory hosts`() {
        assertTrue(transformer.supports("https://jaemin.tistory.com/123"))
        assertFalse(transformer.supports("https://google.com"))
    }

    @Test
    fun `transform successfully`(page: Page) {
        val url = "https://goodprogramer.tistory.com/359"
        page.navigate(url)

        val post = transformer.transform(page)

        println("url : ${post.url}")
        println("title : ${post.title}")
        println("date : ${post.date}")
        println("lang : ${post.lang}")
        println("text : ${post.text}")
    }
}