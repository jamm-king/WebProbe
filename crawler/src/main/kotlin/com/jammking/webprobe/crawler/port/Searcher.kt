package com.jammking.webprobe.crawler.port

interface Searcher {
    suspend fun search(keyword: String): List<String>
}