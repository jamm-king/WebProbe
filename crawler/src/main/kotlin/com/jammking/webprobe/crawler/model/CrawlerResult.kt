package com.jammking.webprobe.crawler.model

import com.jammking.webprobe.data.entity.BlogPost

data class CrawlerResult(
    val pages: List<BlogPost>,
    val stats: CrawlerStats,
    val errors: Map<String, ErrorReason>
)
