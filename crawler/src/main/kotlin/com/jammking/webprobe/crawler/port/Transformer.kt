package com.jammking.webprobe.crawler.port

import com.jammking.webprobe.data.entity.BlogPost
import com.microsoft.playwright.Page

interface Transformer {
    fun supports(url: String): Boolean
    fun transform(page: Page): BlogPost
}