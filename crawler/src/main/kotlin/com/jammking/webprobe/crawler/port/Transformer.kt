package com.jammking.webprobe.crawler.port

import com.jammking.webprobe.data.entity.BlogPost
import com.jammking.webprobe.data.entity.CrawledPage

interface Transformer {
    fun supports(url: String, html: String): Boolean
    fun transform(page: CrawledPage): BlogPost
}