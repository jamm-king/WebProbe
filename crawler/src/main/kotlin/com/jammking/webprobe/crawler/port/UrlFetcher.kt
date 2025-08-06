package com.jammking.webprobe.crawler.port

import com.jammking.webprobe.data.entity.CrawledPage

interface UrlFetcher {
    suspend fun fetch(url: String): CrawledPage
}