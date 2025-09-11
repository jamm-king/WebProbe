package com.jammking.webprobe.crawler.service

import com.jammking.webprobe.common.exception.InvalidSearchRequestException
import com.jammking.webprobe.common.exception.WebProbeException
import com.jammking.webprobe.crawler.adapter.robots.RobotsTxtEvaluator
import com.jammking.webprobe.crawler.exception.*
import com.jammking.webprobe.crawler.model.*
import com.jammking.webprobe.crawler.port.Searcher
import com.jammking.webprobe.crawler.port.Transformer
import com.jammking.webprobe.crawler.port.UrlFetcher
import com.jammking.webprobe.data.entity.BlogPost
import com.jammking.webprobe.data.exception.StorageException
import com.jammking.webprobe.data.service.BlogPostStorage
import com.jammking.webprobe.data.service.UserSeenStorage
import com.microsoft.playwright.Page
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
    private val urlFetcher: UrlFetcher,
    private val transformer: Transformer,
    private val robotsEvaluator: RobotsTxtEvaluator,
    private val blogPostStorage: BlogPostStorage,
    private val userSeenStorage: UserSeenStorage
): Crawler {

    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun crawl(request: SearchRequest): CrawlerResult = coroutineScope {
        validate(request)

        val pagesPerEngine = distributePages(request.maxResults, request.engines.size)
        val errors = Collections.synchronizedMap(mutableMapOf<String, ErrorReason>())

        val allUrls = searchAll(request, pagesPerEngine, errors)

        val (cachedPosts, urlsToFetch) = partitionByCache(allUrls)

        val results = Collections.synchronizedList(cachedPosts.toMutableList())
        fetchTransformAndStore(urlsToFetch, results, errors, request.userId)

        CrawlerResult(
            pages = results,
            stats = CrawlerStats(
                totalUrls = allUrls.size,
                successCount = results.size,
                failureCount = errors.size
            ),
            errors = errors
        )
    }

    private fun validate(request: SearchRequest) {
        if(request.fresh && request.userId == null) {
            throw InvalidSearchRequestException("fresh request requires userId")
        }
    }

    private suspend fun searchAll(
        request: SearchRequest,
        pagesPerEngine: List<Int>,
        errors: MutableMap<String, ErrorReason>
    ) = coroutineScope {
        request.engines.mapIndexed { idx, engine ->
            async {
                val searcher = searcherMap[engine]
                if(searcher == null) {
                    log.warn("No searcher found for $engine")
                    errors["search:$engine"] = ErrorReason.SEARCH_FAILED
                    emptyList()
                } else {
                    val partial = request.copy(engines = listOf(engine), maxResults = pagesPerEngine[idx])
                    try {
                        searcher.search(partial)
                    } catch(e: SearcherException) {
                        log.error("Search failed for engine: $engine, keyword: ${request.keyword}", e)
                        errors["search:$engine"] = ErrorReason.SEARCH_FAILED
                        emptyList()
                    }
                }
            }
        }.awaitAll().flatten()
    }

    private fun partitionByCache(
        urls: List<String>,
    ): Pair<List<BlogPost>, List<String>> {
        val cached = mutableListOf<BlogPost>()
        val toFetch = mutableListOf<String>()

        urls.forEach { url ->
            val page = try {
                blogPostStorage.findByUrl(url)
            } catch(e: StorageException) {
                log.warn("Cache lookup failed for $url", e)
                null
            }

            if(page != null) cached += page else toFetch += url
        }
        return cached to toFetch
    }

    private suspend fun fetchTransformAndStore(
        urlsToFetch: List<String>,
        results: MutableList<BlogPost>,
        errors: MutableMap<String, ErrorReason>,
        userId: String?
    ) = coroutineScope {
        urlsToFetch.map { url ->
            async {
                if (!isAllowedByRobots(url, errors)) return@async

                // first try
                val firstTry = runCatching { attemptOnce(url, results, userId) }
                if (firstTry.isSuccess) return@async
                val firstErr = firstTry.exceptionOrNull()!!
                if (firstErr is WebProbeException) {
                    log.warn("No retry for WebProbeException at $url", firstErr)
                    recordError(url, errors, firstErr)
                    return@async
                }

                // second try
                log.warn("Unknown Exception at $url, retrying once...", firstErr)
                val secondTry = runCatching { attemptOnce(url, results, userId) }
                if (secondTry.isFailure) {
                    val secondErr = secondTry.exceptionOrNull()!!
                    log.error("Retry failed for $url", secondErr)
                    recordError(url, errors, secondErr)
                }
            }
        }.awaitAll()
    }

    private suspend fun attemptOnce(
        url: String,
        results: MutableList<BlogPost>,
        userId: String?
    ) {
        var page: Page? = null
        try {
            // fetch
            page = urlFetcher.fetch(url)
            // transform
            val post = transformer.transform(page)
            // store
            savePost(url, post)

            results += post
            saveUserSeen(userId, url)
        } finally {
            runCatching { page?.close() }
                .onFailure { t -> log.warn("Failed to close Page for $url", t) }
        }
    }

    private fun recordError(url: String, errors: MutableMap<String, ErrorReason>, t: Throwable) {
        val reason = when(t) {
            is FetchFailedException -> ErrorReason.FETCH_FAILED
            is TransformException -> ErrorReason.TRANSFORM_FAILED
            is ParseException -> ErrorReason.PARSING_FAILED
            is RobotsTxtException -> ErrorReason.ROBOTS_TXT_FAILED
            else -> ErrorReason.UNKNOWN
        }
        errors[url] = reason
    }

    private fun isAllowedByRobots(url: String, errors: MutableMap<String, ErrorReason>): Boolean =
        try {
            val u = URL(url)
            val allowed = robotsEvaluator.isAllowed(u.host, u.path, "WebProbeBot")
            if(!allowed) {
                log.info("robots.txt blocked access to $url")
                errors[url] = ErrorReason.ROBOTS_TXT_FAILED
            }
            allowed
        } catch(e: Exception) {
            log.warn("robots.txt check failed for $url", e)
            errors[url] = ErrorReason.ROBOTS_TXT_FAILED
            false
        }

    private fun savePost(url: String, post: BlogPost) {
        try {
            blogPostStorage.save(url, post.title, post.date, post.lang, post.text)
        } catch(e: StorageException) {
            log.warn("Failed to save blog post for $url", e)
        }
    }

    private fun saveUserSeen(userId: String?, url: String) {
        if(userId == null) return
        try {
            userSeenStorage.save(userId, url)
        } catch(e: StorageException) {
            log.warn("Failed to record userSeen for user $userId and url $url", e)
        }
    }

    private fun distributePages(total: Int, parts: Int): List<Int> {
        val base = total / parts
        val remainder = total % parts
        return List(parts) { if (it < remainder) base + 1 else base }
    }
}