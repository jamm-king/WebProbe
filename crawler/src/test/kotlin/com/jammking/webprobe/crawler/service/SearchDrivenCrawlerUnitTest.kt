package com.jammking.webprobe.crawler.service

import com.jammking.webprobe.crawler.adapter.robots.RobotsTxtEvaluator
import com.jammking.webprobe.crawler.adapter.searcher.TistorySearcher
import com.jammking.webprobe.crawler.config.SearcherConfig
import com.jammking.webprobe.crawler.model.SearchEngine
import com.jammking.webprobe.crawler.model.SearchRequest
import com.jammking.webprobe.crawler.port.Searcher
import com.jammking.webprobe.crawler.port.UrlFetcher
import com.jammking.webprobe.crawler.service.resolver.UrlFetcherResolver
import com.jammking.webprobe.data.entity.CrawledPage
import com.jammking.webprobe.data.exception.StorageException
import com.jammking.webprobe.data.service.CrawledPageStorage
import com.jammking.webprobe.data.service.UserSeenStorage
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
class SearchDrivenCrawlerUnitTest {

    @Mock
    lateinit var searcher: TistorySearcher
    @Mock
    lateinit var urlFetcherResolver: UrlFetcherResolver
    @Mock
    lateinit var robotsTxtEvaluator: RobotsTxtEvaluator
    @Mock
    lateinit var crawledPageStorage: CrawledPageStorage
    @Mock
    lateinit var userSeenStorage: UserSeenStorage

    private lateinit var crawler: SearchDrivenCrawler

    @BeforeEach
    fun setup() {
        val searcherMap = SearcherConfig().searcherMap(searcher)
        crawler = SearchDrivenCrawler(
            searcherMap,
            urlFetcherResolver,
            robotsTxtEvaluator,
            crawledPageStorage,
            userSeenStorage
        )
    }

    @Test
    fun `should return cached page when available`() = runTest {
        // given
        val url = "https://test.com/page1"
        val cachedPage = CrawledPage(
            url,
            "Title",
            "<html>...</html>",
            "Text content"
        )
        val request = SearchRequest(
            keyword = "example",
            engines = listOf(SearchEngine.TISTORY),
            maxResults = 1
        )

        whenever(searcher.search(any())).thenReturn(listOf(url))
        whenever(crawledPageStorage.findByUrl(url)).thenReturn(cachedPage)

        // when
        val result = crawler.crawl(request)

        // then
        assertEquals(1, result.pages.size)
        assertEquals(url, result.pages[0].url)
        assertEquals(1, result.stats.successCount)
        assertEquals(0, result.stats.failureCount)

        verify(searcher).search(any())
        verify(crawledPageStorage).findByUrl(url)
        verifyNoMoreInteractions(urlFetcherResolver, robotsTxtEvaluator, userSeenStorage)
    }

    @Test
    fun `should fetch and store only new URL for fresh request`() = runTest {
        // given
        val userId = "user123"
        val cachedUrl = "https://test.com/page1"
        val newUrl = "https://test.com/page2"

        val request = SearchRequest(
            keyword = "example",
            engines = listOf(SearchEngine.TISTORY),
            maxResults = 2,
            userId = userId,
            fresh = true
        )

        whenever(searcher.search(any())).thenReturn(listOf(cachedUrl, newUrl))
        whenever(crawledPageStorage.findByUrl(cachedUrl)).thenReturn(CrawledPage(cachedUrl, "Cached", "<html>Cached</html>", "Cached text"))
        whenever(crawledPageStorage.findByUrl(newUrl)).thenReturn(null)
        whenever(robotsTxtEvaluator.isAllowed(any(), any(), any())).thenReturn(true)

        val fetchedPage = CrawledPage(newUrl, "New Title", "<html>New</html>", "New text")
        val fetcher = mock<UrlFetcher> {
            onBlocking { fetch(newUrl) } doReturn(fetchedPage)
        }
        whenever(urlFetcherResolver.resolve(newUrl)).thenReturn(fetcher)

        // when
        val result = crawler.crawl(request)

        // then
        assertEquals(2, result.pages.size)
        assertTrue(result.pages.any { it.url == cachedUrl })
        assertTrue(result.pages.any { it.url == newUrl })
        assertEquals(2, result.stats.successCount)
        assertEquals(0, result.stats.failureCount)

        verify(searcher).search(any())
        verify(crawledPageStorage).findByUrl(cachedUrl)
        verify(crawledPageStorage).findByUrl(newUrl)
        verify(robotsTxtEvaluator).isAllowed("test.com", "/page2", "WebProbeBot")
        verify(urlFetcherResolver).resolve(newUrl)
        verify(fetcher).fetch(newUrl)
        verify(crawledPageStorage).save(newUrl, "New Title", "<html>New</html>", "New text")
        verify(userSeenStorage).save(userId, newUrl)
    }

    @Test
    fun `should fetch when cache lookup throws StorageException`() = runTest {
        // given
        val url = "https://test.com/page1"
        val request = SearchRequest(
            keyword = "example",
            engines = listOf(SearchEngine.TISTORY),
            maxResults = 1
        )

        whenever(searcher.search(any())).thenReturn(listOf(url))
        whenever(robotsTxtEvaluator.isAllowed("test.com", "/page1", "WebProbeBot")).thenReturn(true)

        val fetchedPage = CrawledPage(url, "Title", "<html>...</html>", "Text")
        val fetcher = mock<UrlFetcher> {
            onBlocking { fetch(url) } doReturn(fetchedPage)
        }
        whenever(urlFetcherResolver.resolve(url)).thenReturn(fetcher)

        // when
        val result = crawler.crawl(request)

        // then
        assertEquals(1, result.pages.size)
        assertEquals(url, result.pages[0].url)
        assertEquals(1, result.stats.successCount)
        assertEquals(0, result.stats.failureCount)

        verify(searcher).search(any())
        verify(crawledPageStorage).findByUrl(url)
        verify(robotsTxtEvaluator).isAllowed("test.com", "/page1", "WebProbeBot")
        verify(urlFetcherResolver).resolve(url)
        verify(fetcher).fetch(url)
        verify(crawledPageStorage).save(url, "Title", "<html>...</html>", "Text")
    }

    @Test
    fun `should include page even if storage save throws StorageException`() = runTest {
        // given
        val url = "https://test.com/page2"
        val request = SearchRequest(
            keyword = "example",
            engines = listOf(SearchEngine.TISTORY),
            maxResults = 1
        )

        whenever(searcher.search(any())).thenReturn(listOf(url))
        whenever(crawledPageStorage.findByUrl(url)).thenReturn(null)
        whenever(robotsTxtEvaluator.isAllowed("test.com", "/page2", "WebProbeBot")).thenReturn(true)

        val fetchedPage = CrawledPage(url, "New Title", "<html>New</html>", "New text")
        val fetcher = mock<UrlFetcher> {
            onBlocking { fetch(url) } doReturn(fetchedPage)
        }
        whenever(urlFetcherResolver.resolve(url)).thenReturn(fetcher)
        whenever(crawledPageStorage.save(url, "New Title", "<html>New</html>", "New text"))
            .thenThrow(StorageException("Save failed"))

        // when
        val result = crawler.crawl(request)

        // then
        assertEquals(1, result.pages.size)
        assertEquals(url, result.pages[0].url)
        assertEquals(1, result.stats.successCount)
        assertEquals(0, result.stats.failureCount)

        verify(fetcher).fetch(url)
        verify(crawledPageStorage).save(url, "New Title", "<html>New</html>", "New text")
    }

    @Test
    fun `should ignore userSeen save failure`() = runTest {
        // given
        val userId = "user123"
        val url = "https://test.com/page3"
        val request = SearchRequest(
            keyword = "example",
            engines = listOf(SearchEngine.TISTORY),
            maxResults = 1,
            userId = userId,
            fresh = true
        )

        whenever(searcher.search(any())).thenReturn(listOf(url))
        whenever(crawledPageStorage.findByUrl(url)).thenReturn(null)
        whenever(robotsTxtEvaluator.isAllowed("test.com", "/page3", "WebProbeBot")).thenReturn(true)

        val fetchedPage = CrawledPage(url, "T", "<html></html>", "txt")
        val fetcher = mock<UrlFetcher> {
            onBlocking { fetch(url) } doReturn(fetchedPage)
        }
        whenever(urlFetcherResolver.resolve(url)).thenReturn(fetcher)
        whenever(userSeenStorage.save(userId, url))
            .thenThrow(StorageException("userSeen save failed"))

        // when
        val result = crawler.crawl(request)

        // then
        assertEquals(1, result.pages.size)
        assertEquals(url, result.pages[0].url)
        assertEquals(1, result.stats.successCount)
        assertEquals(0, result.stats.failureCount)

        verify(userSeenStorage).save(userId, url)
    }
}