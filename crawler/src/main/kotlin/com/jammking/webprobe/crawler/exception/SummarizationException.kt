package com.jammking.webprobe.crawler.exception

import com.jammking.webprobe.common.exception.WebProbeException

class SummarizationException(
    val url: String,
    cause: Throwable? = null
) : WebProbeException("Failed to summarize page from $url", cause)