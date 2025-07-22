package com.jammking.webprobe.crawler.adapter.searcher

import com.jammking.webprobe.crawler.exception.SearcherException
import com.jammking.webprobe.crawler.port.Searcher
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URLEncoder

@Component
class GoogleSearcher : Searcher {
    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun search(keyword: String): List<String> {
        delay(1000)

        try {
            val encodedKeyword = URLEncoder.encode(keyword, Charsets.UTF_8)
            val queryUrl = "https://www.google.com/search?q=$encodedKeyword"

            val doc = Jsoup.connect(queryUrl)
                .userAgent(USER_AGENT)
                .timeout(5000)
                .get()

            return doc.select("a[ref]")
                .mapNotNull { element ->
                    val href = element.attr("href")
                    parseGoogleHref(href)
                }
                .distinct()
        } catch(e: Exception) {
            throw SearcherException("GoogleSearcher", keyword, e)
        }
    }

    private fun parseGoogleHref(href: String): String? {
        return if (href.startsWith("/url?q=")) {
            href.removePrefix("/url?q=").substringBefore("&")
        } else null
    }

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (compatible; WebProbeBot/1.0; +https://github.com/jamm-king/WebProbe)"
    }
}