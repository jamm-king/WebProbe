package com.jammking.webprobe.crawler.exception

import com.jammking.webprobe.common.exception.WebProbeException

class ParseException(
    val url: String,
    val reason: String
) : WebProbeException("Failed to parse HTML from $url: $reason")