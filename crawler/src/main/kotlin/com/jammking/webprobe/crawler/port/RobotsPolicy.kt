package com.jammking.webprobe.crawler.port

interface RobotsPolicy {
    fun isAllowed(userAgent: String, path: String): Boolean
}