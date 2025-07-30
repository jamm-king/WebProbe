package com.jammking.webprobe.crawler.exception

import com.jammking.webprobe.common.exception.WebProbeException
import com.jammking.webprobe.crawler.model.SearchEngine

class SearcherException(
    val engine: SearchEngine,
    val keyword: String,
    cause: Throwable? = null
) : WebProbeException("Failed to search keyword '$keyword' via $engine", cause)