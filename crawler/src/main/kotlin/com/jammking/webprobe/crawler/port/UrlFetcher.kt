package com.jammking.webprobe.crawler.port

import com.microsoft.playwright.Page

interface UrlFetcher {
    suspend fun fetch(url: String): Page
}