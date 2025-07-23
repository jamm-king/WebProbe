package com.jammking.webprobe.crawler.exception

import com.jammking.webprobe.common.exception.WebProbeException

class RobotsTxtException(
    val domain: String,
    val reason: String,
    cause: Throwable? = null
): WebProbeException("Failed to handle robots.txt from $domain: $reason", cause)