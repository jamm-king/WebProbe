package com.jammking.webprobe.crawler.adapter.robots

import com.jammking.webprobe.common.http.HttpClient
import com.jammking.webprobe.common.http.OkHttpClientWrapper
import com.jammking.webprobe.crawler.exception.RobotsTxtException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class RobotsTxtFetcher {

    private val httpClient : HttpClient = OkHttpClientWrapper()

    private val log = LoggerFactory.getLogger(this::class.java)

    fun fetch(domain: String): String? {
        val url = "https://$domain/robots.txt"

        return try {
            val response = httpClient.get(url)
            if(response.statusCode in 200..299) {
                log.debug("Fetched robots.txt from $url")
                response.body
            } else {
                log.warn("robots.txt returned ${response.statusCode} for $url")
                null
            }
        } catch(e: Exception) {
            log.error("Failed to fetch robots.txt from $url", e)
            throw RobotsTxtException(domain, "fetch failed", e)
        }
    }
}