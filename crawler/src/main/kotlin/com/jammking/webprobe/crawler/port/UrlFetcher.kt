package com.jammking.webprobe.crawler.port

import com.jammking.webprobe.crawler.model.CrawledPage

interface UrlFetcher {
    suspend fun fetch(url: String): CrawledPage
}