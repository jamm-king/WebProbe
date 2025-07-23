package com.jammking.webprobe.crawler.service

import com.jammking.webprobe.crawler.exception.FetchFailedException
import com.jammking.webprobe.crawler.exception.ParseException
import com.jammking.webprobe.crawler.exception.SearcherException
import com.jammking.webprobe.crawler.model.*
import com.jammking.webprobe.crawler.port.Searcher
import com.jammking.webprobe.crawler.service.resolver.UrlFetcherResolver
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class KeywordBasedCrawler(
    private val searcherMap: Map<SearchEngine, Searcher>,
    private val urlFetcherResolver: UrlFetcherResolver
): Crawler {

    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun crawl(request: SearchRequest): CrawlerResult = coroutineScope {
        val engines = request.engines
        val totalPage = request.maxResults
        val pagesPerEngine = distributePages(totalPage, engines.size)

        val allUrls = Collections.synchronizedList(mutableListOf<String>())
        val errors = Collections.synchronizedMap(mutableMapOf<String, ErrorReason>())

        engines.mapIndexed { idx, engine ->
            async {
                val searcher = searcherMap[engine]
                if(searcher == null) {
                    log.warn("No searcher found for $engine")
                    errors["search:$engine"] = ErrorReason.SEARCH_FAILED
                    return@async
                }

                val partialRequest = request.copy(
                    engines = listOf(engine),
                    maxResults = pagesPerEngine[idx]
                )

                try {
                    val urls = searcher.search(partialRequest)
                    allUrls.addAll(urls)
                } catch(e: SearcherException) {
                    log.error("Search failed for engine: $engine, keyword: ${request.keyword}", e)
                    errors["search:$engine"] = ErrorReason.SEARCH_FAILED
                }
            }
        }.awaitAll()

        val pages = Collections.synchronizedList(mutableListOf<CrawledPage>())
        val fetchJobs = allUrls.map { url ->
            async {
                try {
                    val fetcher = urlFetcherResolver.resolve(url)
                    val page = fetcher.fetch(url)
                    pages.add(page)
                } catch(e: FetchFailedException) {
                    log.warn("Fetch failed for $url", e)
                    errors[url] = ErrorReason.FETCH_FAILED
                } catch(e: ParseException) {
                    log.warn("Parse failed for $url", e)
                    errors[url] = ErrorReason.PARSING_FAILED
                } catch(e: Exception) {
                    log.error("Unknown error for $url", e)
                    errors[url] = ErrorReason.UNKNOWN
                }
            }
        }

        fetchJobs.awaitAll()

        return@coroutineScope CrawlerResult(
            pages = pages,
            stats = CrawlerStats(
                totalUrls = allUrls.size,
                successCount = pages.size,
                failureCount = errors.size
            ),
            errors = errors
        )
    }

    private fun distributePages(total: Int, parts: Int): List<Int> {
        val base = total / parts
        val remainder = total % parts
        return List(parts) { if (it < remainder) base + 1 else base }
    }
}