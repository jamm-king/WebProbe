package com.jammking.webprobe.crawler.adapter.searcher

import com.jammking.webprobe.crawler.exception.SearcherException
import com.jammking.webprobe.crawler.model.SearchEngine
import com.jammking.webprobe.crawler.model.SearchRequest
import com.jammking.webprobe.crawler.port.Searcher
import com.jammking.webprobe.crawler.port.UrlFetcher
import com.jammking.webprobe.crawler.service.resolver.UrlFetcherResolver
import com.jammking.webprobe.data.exception.StorageException
import com.jammking.webprobe.data.service.UserSeenStorage
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URLEncoder

@Component
class TistorySearcher(
    private val urlFetcherResolver: UrlFetcherResolver,
    private val userSeenStorage: UserSeenStorage
): Searcher {

    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun search(request: SearchRequest): List<String> {
        val keyword = request.keyword
        val maxResults = request.maxResults
        val fresh = request.fresh
        val userId = request.userId

        val collected = mutableSetOf<String>()
        var page = 1

        while(collected.size < maxResults) {
            delay(1000)

            val url = buildSearchUrl(keyword, page)
            log.debug("fetching Tistory search page: $url")

            val html = try {
                val urlFetcher = urlFetcherResolver.resolve(url)
                urlFetcher.fetch(url).html
            } catch(e: Exception) {
                throw SearcherException(SearchEngine.TISTORY, keyword, e)
            }

            val doc = Jsoup.parse(html)
            val urls = doc.select("a.link_cont")
                .mapNotNull { it.attr("href") }
                .filter { it.startsWith("http") }

            if(urls.isEmpty()) {
                log.debug("No more results on page $page for keyword '$keyword'")
                break
            }

            val newUrls = if(fresh && userId != null) {
                try {
                    urls.filterNot { userSeenStorage.isSeen(userId, it) }
                } catch(e: StorageException) {
                    log.warn("userSeen check failed for user $userId", e)
                    urls
                }
            } else {
                urls
            }

            val added = newUrls.filterNot { it in collected }
            collected.addAll(added)

            if(added.isEmpty()) {
                log.debug("No new unvisited URLs found on page $page")
                break
            }

            page++
        }

        return collected.take(maxResults)
    }

    private fun buildSearchUrl(keyword: String, page: Int): String {
        val encoded = URLEncoder.encode(keyword, Charsets.UTF_8)
        return "https://www.tistory.com/search?keyword=$encoded&page=$page"
    }
}