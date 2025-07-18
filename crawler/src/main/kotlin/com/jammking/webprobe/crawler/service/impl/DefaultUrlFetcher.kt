package com.jammking.webprobe.crawler.service.impl

import com.jammking.webprobe.crawler.exception.FetchFailedException
import com.jammking.webprobe.crawler.exception.ParseException
import com.jammking.webprobe.crawler.model.CrawledPage
import com.jammking.webprobe.crawler.port.UrlFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.io.IOException

class DefaultUrlFetcher(
    private val client: OkHttpClient = OkHttpClient()
) : UrlFetcher {
    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun fetch(url: String): CrawledPage = withContext(Dispatchers.IO) {
        log.info("Fetching page: $url")

        try {
            val response = client.newCall(Request.Builder().url(url).build()).execute()
            val html = response.body?.string()
                ?: throw FetchFailedException(url)

            val doc = Jsoup.parse(html)
            val title = doc.title()
            val text = doc.body().text() ?: ""

            return@withContext CrawledPage(
                url = url,
                title = title,
                html = html,
                text = text
            )
        } catch (e: IOException) {
            throw FetchFailedException(url, e)
        } catch (e: Exception) {
            throw ParseException(url, e.message ?: "unknown error")
        }
    }
}