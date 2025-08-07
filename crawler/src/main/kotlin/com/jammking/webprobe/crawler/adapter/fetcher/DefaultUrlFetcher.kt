package com.jammking.webprobe.crawler.adapter.fetcher

import com.jammking.webprobe.common.http.HttpClient
import com.jammking.webprobe.crawler.exception.FetchFailedException
import com.jammking.webprobe.crawler.port.UrlFetcher
import com.jammking.webprobe.data.entity.CrawledPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DefaultUrlFetcher(
    private val httpClient: HttpClient
) : UrlFetcher {

    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun fetch(url: String): CrawledPage = withContext(Dispatchers.IO) {
        log.info("Fetching page: $url")

        try {
            val response = httpClient.get(url)
            val html = response.body ?: throw FetchFailedException(url)

            val doc = Jsoup.parse(html)
            val title = doc.title()
            val text = doc.body().text()

            return@withContext CrawledPage(
                url = url,
                title = title,
                html = html,
                text = text
            )
        } catch (e: FetchFailedException) {
            throw e
        } catch (e: Exception) {
            throw FetchFailedException(url, e)
        }
    }
}