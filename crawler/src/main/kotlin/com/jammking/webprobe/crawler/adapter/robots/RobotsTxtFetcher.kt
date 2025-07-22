package com.jammking.webprobe.crawler.adapter.robots

import com.jammking.webprobe.common.http.HttpClient
import com.jammking.webprobe.common.http.OkHttpClientWrapper
import org.springframework.stereotype.Component

@Component
class RobotsTxtFetcher {

    private val httpClient : HttpClient = OkHttpClientWrapper()

    fun fetch(domain: String): String? {
        val url = "https://$domain/robots.txt"
        val response = httpClient.get(url)

        return if (response.statusCode in 200..299) response.body else null
    }
}