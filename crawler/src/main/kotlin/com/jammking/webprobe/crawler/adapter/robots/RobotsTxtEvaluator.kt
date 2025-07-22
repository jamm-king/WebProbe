package com.jammking.webprobe.crawler.adapter.robots

import org.springframework.stereotype.Component

@Component
class RobotsTxtEvaluator(
    private val fetcher: RobotsTxtFetcher,
    private val parser: RobotsTxtParser
) {
    fun isAllowed(
        domain: String,
        path: String,
        userAgent: String
    ): Boolean {
        val robotsTxt = fetcher.fetch(domain) ?: return false
        val policy = parser.parse(robotsTxt)

        return policy.isAllowed(userAgent, path)
    }
}