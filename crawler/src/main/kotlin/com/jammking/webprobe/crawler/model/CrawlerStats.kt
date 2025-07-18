package com.jammking.webprobe.crawler.model

data class CrawlerStats(
    val totalUrls: Int,
    val successCount: Int,
    val failureCount: Int
)
