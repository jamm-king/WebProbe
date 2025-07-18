package com.jammking.webprobe.crawler.service

import com.jammking.webprobe.crawler.exception.FetchFailedException
import com.jammking.webprobe.crawler.exception.ParseException
import com.jammking.webprobe.crawler.exception.SearcherException
import com.jammking.webprobe.crawler.exception.SummarizationException
import com.jammking.webprobe.crawler.model.CrawledPage
import com.jammking.webprobe.crawler.model.CrawlerResult
import com.jammking.webprobe.crawler.model.CrawlerStats
import com.jammking.webprobe.crawler.model.ErrorReason
import com.jammking.webprobe.crawler.port.Searcher
import com.jammking.webprobe.crawler.port.UrlFetcher
import com.jammking.webprobe.crawler.service.resolver.UrlFetcherResolver
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class KeywordBasedCrawler(
    private val searcher: Searcher,
    private val urlFetcherResolver: UrlFetcherResolver
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun crawl(keyword: String): CrawlerResult = coroutineScope {
        val urls = try {
            searcher.search(keyword)
        } catch (e: SearcherException) {
            log.error("Search failed for keyword: $keyword", e)
            return@coroutineScope CrawlerResult(
                pages = emptyList(),
                stats = CrawlerStats(0, 0, 0),
                errors = mapOf("search:$keyword" to ErrorReason.SEARCH_FAILED)
            )
        }

        val pages = Collections.synchronizedList(mutableListOf<CrawledPage>())
        val errors = Collections.synchronizedMap(mutableMapOf<String, ErrorReason>())

        val fetchJobs = urls.map { url ->
            async {
                try {
                    val urlFetcher = urlFetcherResolver.resolve(url)
                    val page = urlFetcher.fetch(url)
                    pages.add(page)
                } catch (e: FetchFailedException) {
                    log.warn("Fetch failed for $url", e)
                    errors[url] = ErrorReason.FETCH_FAILED
                } catch (e: ParseException) {
                    log.warn("Parse failed for $url", e)
                    errors[url] = ErrorReason.PARSING_FAILED
                } catch (e: SummarizationException) {
                    log.warn("Summarization failed for $url", e)
                    errors[url] = ErrorReason.SUMMARY_FAILED
                } catch (e: Exception) {
                    log.error("Unknown error for $url", e)
                    errors[url] = ErrorReason.UNKNOWN
                }
            }
        }

        fetchJobs.awaitAll()

        return@coroutineScope CrawlerResult(
            pages = pages,
            stats = CrawlerStats(
                totalUrls = urls.size,
                successCount = pages.size,
                failureCount = errors.size
            ),
            errors = errors
        )
    }

}