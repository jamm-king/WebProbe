package com.jammking.webprobe.crawler.adapter.robots

import com.jammking.webprobe.crawler.exception.RobotsTxtException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class RobotsTxtEvaluator(
    private val fetcher: RobotsTxtFetcher,
    private val parser: RobotsTxtParser
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun isAllowed(
        domain: String,
        path: String,
        userAgent: String
    ): Boolean {
        return try {
            val robotsTxt = fetcher.fetch(domain)
            if(robotsTxt == null) {
                log.info("robots.txt not found for $domain, disallowing by default")
                false
            } else {
                val policy = parser.parse(robotsTxt)
                val allowed = policy.isAllowed(userAgent, path)
                log.debug("robots.txt evaluated for $domain: path=$path, allowed=$allowed")
                allowed
            }
        } catch(e: RobotsTxtException) {
            log.warn("robots.txt processing failed for $domain, disallowing by default: ${e.reason}")
            false
        }
    }
}