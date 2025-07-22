package com.jammking.webprobe.crawler.adapter.robots

import com.jammking.webprobe.crawler.port.RobotsPolicy

class SimpleRobotsPolicy(
    private val disallowRules: Map<String, List<String>>
): RobotsPolicy {

    override fun isAllowed(userAgent: String, path: String): Boolean {
        val normalizedPath = if(!path.startsWith("/")) "/$path" else path
        val agent = userAgent.lowercase()

        val rules = when {
            disallowRules.containsKey(agent) -> { disallowRules[agent] }
            disallowRules.containsKey("*") -> { disallowRules["*"] }
            else -> null
        } ?: return false

        val isBlocked = rules.any { rule -> normalizedPath.startsWith(rule) }

        return !isBlocked
    }
}