package com.jammking.webprobe.crawler.model

import com.jammking.webprobe.data.entity.CrawledPage

data class CrawlerResult(
    val pages: List<CrawledPage>,
    val stats: CrawlerStats,
    val errors: Map<String, ErrorReason>
)
