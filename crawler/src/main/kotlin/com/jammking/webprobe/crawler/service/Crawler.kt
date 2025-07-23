package com.jammking.webprobe.crawler.service

import com.jammking.webprobe.crawler.model.CrawlerResult
import com.jammking.webprobe.crawler.model.SearchRequest

interface Crawler {
    suspend fun crawl(request: SearchRequest): CrawlerResult
}