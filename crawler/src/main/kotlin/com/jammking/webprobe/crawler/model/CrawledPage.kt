package com.jammking.webprobe.crawler.model

data class CrawledPage(
    val url: String,
    val title: String,
    val html: String,
    val text: String,
    val summary: String? = null
)
