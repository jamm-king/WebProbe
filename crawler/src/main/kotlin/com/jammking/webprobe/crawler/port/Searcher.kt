package com.jammking.webprobe.crawler.port

import com.jammking.webprobe.crawler.model.SearchRequest

interface Searcher {
    suspend fun search(request: SearchRequest): List<String>
}