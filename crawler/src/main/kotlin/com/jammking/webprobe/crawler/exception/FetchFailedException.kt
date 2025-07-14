package com.jammking.webprobe.crawler.exception

import com.jammking.webprobe.common.exception.WebProbeException

class FetchFailedException(
    val url: String,
    cause: Throwable? = null
) : WebProbeException("Failed to fetch page from $url, cause")