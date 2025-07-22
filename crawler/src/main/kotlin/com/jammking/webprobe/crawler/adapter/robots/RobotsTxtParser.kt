package com.jammking.webprobe.crawler.adapter.robots

import com.jammking.webprobe.crawler.port.RobotsPolicy
import org.springframework.stereotype.Component

@Component
class RobotsTxtParser {

    fun parse(raw: String): RobotsPolicy {
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

        return SimpleRobotsPolicy(rules)
    }
}