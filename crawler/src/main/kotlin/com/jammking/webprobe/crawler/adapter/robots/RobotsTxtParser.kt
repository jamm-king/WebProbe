package com.jammking.webprobe.crawler.adapter.robots

import com.jammking.webprobe.crawler.exception.RobotsTxtException
import com.jammking.webprobe.crawler.port.RobotsPolicy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class RobotsTxtParser {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun parse(raw: String): RobotsPolicy {
        return try {
            val rules = mutableMapOf<String, MutableList<String>>()
            var currentAgent: String? = null

            raw.lines().forEach { line ->
                val trimmed = line.trim().lowercase()
                when {
                    trimmed.startsWith("user-agent") -> {
                        val agent = trimmed.removePrefix("user-agent:").trim()
                        currentAgent = agent
                        rules.putIfAbsent(agent, mutableListOf())
                    }
                    trimmed.startsWith("disallow:") && currentAgent != null -> {
                        val path = trimmed.removePrefix("disallow:").trim()
                        if(path.isNotEmpty()) {
                            rules[currentAgent]?.add(path)
                        }
                    }
                }
            }

            log.debug("Parsed robots.txt with agents: {}", rules.keys)
            SimpleRobotsPolicy(rules)
        } catch(e: Exception) {
            log.error("Failed to parse robots.txt", e)
            throw RobotsTxtException("unknown", "parse failed", e)
        }
    }
}