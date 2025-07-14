package com.jammking.webprobe.crawler.service.impl

import com.jammking.webprobe.crawler.port.Searcher
import org.slf4j.LoggerFactory

class GoogleSearcher : Searcher {
    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun search(keyword: String): List<String> {
        TODO("Not yet implemented")
    }
}