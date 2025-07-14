package com.jammking.webprobe.crawler.exception

import com.jammking.webprobe.common.exception.WebProbeException

class SearcherException(
    val engine: String,
    val keyword: String,
    cause: Throwable? = null
) : WebProbeException("Failed to search keyword '$keyword' via $engine", cause)