package com.jammking.webprobe.crawler.adapter.searcher

import com.jammking.webprobe.crawler.exception.SearcherException
import com.jammking.webprobe.crawler.model.SearchRequest
import com.jammking.webprobe.crawler.port.Searcher
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URLEncoder

@Component
class GoogleSearcher : Searcher {
    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun search(request: SearchRequest): List<String> {
        val keyword = request.keyword
        val maxResults = request.maxResults

        val results = mutableListOf<String>()
        var page = 0

        while(results.size < maxResults) {
            delay(1000)

            try {
                val start = page * 10
                val encoded = URLEncoder.encode(keyword, Charsets.UTF_8)
                val queryUrl = "https://www.google.com/search?q=$encoded&start=$start"

                val doc = Jsoup.connect(queryUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get()

                val urls = doc.select("a[ref]")
                    .mapNotNull { parseGoogleHref(it.attr("href")) }
                    .filter { it !in results }

                if(urls.isEmpty()) break

                results.addAll(urls)
                page++
            } catch(e: Exception) {
                log.warn("Failed to fetch Google page $page for keyword: $keyword", e)
                throw SearcherException("GoogleSearcher", keyword, e)
            }
        }

        return results.take(maxResults)
    }

    private fun parseGoogleHref(href: String): String? {
        return if (href.startsWith("/url?q=")) {
            href.removePrefix("/url?q=").substringBefore("&")
        } else null
    }

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (compatible; WebProbeBot/1.0; +https://github.com/jamm-king/WebProbe)"
        private const val TIMEOUT = 5000
    }
}