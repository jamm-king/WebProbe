package com.jammking.webprobe.crawler.service

import com.jammking.webprobe.common.exception.InvalidSearchRequestException
import com.jammking.webprobe.crawler.adapter.robots.RobotsTxtEvaluator
import com.jammking.webprobe.crawler.exception.FetchFailedException
import com.jammking.webprobe.crawler.exception.ParseException
import com.jammking.webprobe.crawler.exception.SearcherException
import com.jammking.webprobe.crawler.model.*
import com.jammking.webprobe.crawler.port.Searcher
import com.jammking.webprobe.crawler.service.resolver.UrlFetcherResolver
import com.jammking.webprobe.data.entity.CrawledPage
import com.jammking.webprobe.data.exception.StorageException
import com.jammking.webprobe.data.service.CrawledPageStorage
import com.jammking.webprobe.data.service.UserSeenStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URL
import java.util.*

@Service
class SearchDrivenCrawler(
    private val searcherMap: Map<SearchEngine, Searcher>,
    private val urlFetcherResolver: UrlFetcherResolver,
    private val robotsEvaluator: RobotsTxtEvaluator,
    private val crawledPageStorage: CrawledPageStorage,
    private val userSeenStorage: UserSeenStorage
): Crawler {

    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun crawl(request: SearchRequest): CrawlerResult = coroutineScope {
        val userId = request.userId
        val fresh = request.fresh

        if(fresh && userId == null) {
            throw InvalidSearchRequestException("fresh request required userId")
        }

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

        val cachedPages = Collections.synchronizedList(mutableListOf<CrawledPage>())
        val urlsToFetch = Collections.synchronizedList(mutableListOf<String>())

        allUrls.forEach { url ->
            val cachedPage = try {
                crawledPageStorage.findByUrl(url)
            } catch(e: StorageException) {
                log.warn("Cache lookup failed for $url", e)
                null
            }
            if(cachedPage != null) {
                cachedPages.add(cachedPage)
            } else {
                urlsToFetch.add(url)
            }
        }

        val pages = Collections.synchronizedList(cachedPages)
        val fetchJobs = urlsToFetch.mapNotNull { url ->
            val allowed = try {
                val parsedUrl = URL(url)
                val domain = parsedUrl.host
                val path = parsedUrl.path
                robotsEvaluator.isAllowed(domain, path, "WebProbeBot")
            } catch(e: Exception) {
                log.warn("robots.txt check failed for $url", e)
                errors[url] = ErrorReason.ROBOTS_TXT_FAILED
                false
            }

            if(!allowed) {
                log.info("robots.txt blocked access to $url")
                errors[url] = ErrorReason.ROBOTS_TXT_FAILED
                null
            } else {
                async {
                    try {
                        val fetcher = urlFetcherResolver.resolve(url)
                        val page = fetcher.fetch(url)

                        try {
                            crawledPageStorage.save(url, page.title, page.html, page.text)
                        } catch(e: StorageException) {
                            log.warn("Failed to save crawled page for $url", e)
                        }
                        pages.add(page)

                        userId?.let {
                            try {
                                userSeenStorage.save(it, url)
                            } catch(e: StorageException) {
                                log.warn("Failed to record userSeen for user $userId and url $url", e)
                            }
                        }
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