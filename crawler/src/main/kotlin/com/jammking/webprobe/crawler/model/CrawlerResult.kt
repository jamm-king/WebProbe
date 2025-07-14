package com.jammking.webprobe.crawler.model

data class CrawlerResult(
    val pages: List<CrawledPage>,
    val stats: CrawlerStats,
    val errors: Map<String, ErrorReason>
)
