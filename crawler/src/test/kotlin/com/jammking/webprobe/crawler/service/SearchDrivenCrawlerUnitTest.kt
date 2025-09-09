package com.jammking.webprobe.crawler.service

import com.jammking.webprobe.common.exception.InvalidSearchRequestException
import com.jammking.webprobe.crawler.adapter.robots.RobotsTxtEvaluator
import com.jammking.webprobe.crawler.exception.RobotsTxtException
import com.jammking.webprobe.crawler.model.ErrorReason
import com.jammking.webprobe.crawler.model.SearchEngine
import com.jammking.webprobe.crawler.model.SearchRequest
import com.jammking.webprobe.crawler.port.Searcher
import com.jammking.webprobe.crawler.port.Transformer
import com.jammking.webprobe.crawler.port.UrlFetcher
import com.jammking.webprobe.data.entity.BlogPost
import com.jammking.webprobe.data.service.BlogPostStorage
import com.jammking.webprobe.data.service.UserSeenStorage
import com.microsoft.playwright.Page
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.time.Instant

class SearchDrivenCrawlerUnitTest {

    private val searcher = mock<Searcher>()
    private val urlFetcher = mock<UrlFetcher>()
    private val transformer = mock<Transformer>()
    private val robots = mock<RobotsTxtEvaluator>()
    private val storage = mock<BlogPostStorage>()
    private val userSeen = mock<UserSeenStorage>()

    private val engine = SearchEngine.TISTORY

    private fun crawler() = SearchDrivenCrawler(
        searcherMap = mapOf(engine to searcher),
        urlFetcher = urlFetcher,
        transformer = transformer,
        robotsEvaluator = robots,
        blogPostStorage = storage,
        userSeenStorage = userSeen
    )

    private fun req(
        keyword: String = "k",
        engines: List<SearchEngine> = listOf(engine),
        max: Int = 5,
        userId: String? = "u1",
        fresh: Boolean = false
    ) = SearchRequest(
        keyword = keyword,
        engines = engines,
        maxResults = max,
        userId = userId,
        fresh = fresh
    )

    private fun post(url: String = "https://t1", title: String = "t") =
        BlogPost.of(
            url = url,
            title = title,
            date = Instant.now(),
            lang = "ko",
            text = "body"
        )

    @Test
    fun `fresh request without userId throws Exception`() = runTest {
        val r = req(fresh = true, userId = null)
        assertThrows<InvalidSearchRequestException> {
            crawler().crawl(r)
        }
    }

//    @Test
//    fun `maxResults should be distributed according to search engines`() = runTest {
//        whenever(searcherA.search(any())).thenReturn(listOf("https://a1", "https://a2"))
//        whenever(searcherB.search(any())).thenReturn(listOf("https://b1"))
//        whenever(storage.findByUrl(any())).thenReturn(null)
//        whenever(robots.isAllowed(any(), any(), any())).thenReturn(true)
//
//        val page = mock<Page>()
//        whenever(urlFetcher.fetch(any())).thenReturn(page)
//        whenever(transformer.transform(page)).thenReturn(post(url = "https://a1"))
//
//        whenever(transformer.transform(any())).thenReturn(post(url = "https://a1"))
//
//        val r = req(max = 5)
//        crawler().crawl(r)
//
//        argumentCaptor<SearchRequest>().apply {
//            verify(searcherA).search(capture())
//            verify(searcherB).search(capture())
//            val maxes = allValues.map { it.maxResults }.sortedDescending()
//            assert(maxes == listOf(3, 2))
//        }
//    }

    @Test
    fun `when cache hit should skip fetch-transform and include cache`() = runTest {
        val url = "https://cached"
        whenever(searcher.search(any())).thenReturn(listOf(url))
        whenever(storage.findByUrl(url)).thenReturn(post(url))
        val r = req(engines = listOf(engine), max = 1)

        val result = crawler().crawl(r)

        verify(urlFetcher, never()).fetch(any())
        verify(transformer, never()).transform(any())

        assertEquals(1, result.pages.size)
        assertEquals(1, result.stats.successCount)
        assertEquals(0, result.stats.failureCount)
    }

    @Test
    fun `when robots disallow record error and do not fetch`() = runTest {
        val url = "https://blocked/some"
        whenever(searcher.search(any())).thenReturn(listOf(url))
        whenever(storage.findByUrl(url)).thenReturn(null)
        whenever(robots.isAllowed(any(), any(), any())).thenReturn(false)

        val out = crawler().crawl(req(max = 1))
        assert(out.errors[url] == ErrorReason.ROBOTS_TXT_FAILED)
        assert(out.stats.failureCount == 1)
        assert(out.pages.isEmpty())
    }

    @Test
    fun `when robots evaluator throws exception record error`() = runTest {
        val url = "https://boom"
        whenever(searcher.search(any())).thenReturn(listOf(url))
        whenever(storage.findByUrl(url)).thenReturn(null)
        whenever(robots.isAllowed(any(), any(), any())).thenThrow(RobotsTxtException("boom", "boom"))

        val out = crawler().crawl(req(max = 1))
        assert(out.errors[url] == ErrorReason.ROBOTS_TXT_FAILED)
        assert(out.pages.isEmpty())
    }

    @Test
    fun `fetch, transform, store, userSeen, page close successfully`() = runTest {
        val url = "https://ok"
        val pg = mock<Page>()
        val bp = post(url, "ok-title")

        whenever(searcher.search(any())).thenReturn(listOf(url))
        whenever(storage.findByUrl(url)).thenReturn(null)
        whenever(robots.isAllowed(any(), any(), any())).thenReturn(true)
        whenever(urlFetcher.fetch(url)).thenReturn(pg)
        whenever(transformer.transform(pg)).thenReturn(bp)

        val out = crawler().crawl(req(max = 1, userId = "u-123"))

        verify(storage).save(eq(url), eq(bp.title), eq(bp.date), eq(bp.lang), eq(bp.text))
        verify(userSeen).save(eq("u-123"), eq(url))
        verify(pg).close()

        assertEquals(1, out.pages.size)
        assertEquals(url, out.pages.first().url)
        assertTrue(out.errors.isEmpty())
    }
}