package com.jammking.webprobe.crawler.model

data class SearchRequest(
    val keyword: String,
    val engines: List<SearchEngine>,
    val maxResults: Int = 5
)
