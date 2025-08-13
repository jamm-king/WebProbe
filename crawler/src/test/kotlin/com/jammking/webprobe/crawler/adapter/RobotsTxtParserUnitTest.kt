package com.jammking.webprobe.crawler.adapter

import com.jammking.webprobe.crawler.adapter.robots.RobotsTxtParser
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RobotsTxtParserUnitTest {

    private lateinit var parser: RobotsTxtParser

    @BeforeEach
    fun setup() {
        parser = RobotsTxtParser()
    }

    @Test
    fun `should disallow path for user-agent star`() {
        // given
        val robotsTxt = """
            User-agent: *
            Disallow: /private
        """.trimIndent()

        val policy = parser.parse(robotsTxt)

        // when & then
        assertFalse(policy.isAllowed("WebProbe", "/private/data")     )
        assertTrue(policy.isAllowed("WebProbe", "/public"))
    }

    @Test
    fun `should prefer specific user-agent over star`() {
        // given
        val robotsTxt = """
            User-agent: *
            Disallow: /blocked
            
            User-agent: WebProbe
            Disallow: /webprobe-only
        """.trimIndent()

        val policy = parser.parse(robotsTxt)

        // when & then
        assertTrue(policy.isAllowed("WebProbe", "/blocked"))
        assertFalse(policy.isAllowed("WebProbe", "/webprobe-only"))
    }

    @Test
    fun `should allow all when disallow is empty`() {
        // given
        val robotsTxt = """
            User-agent: WebProbe
            Disallow: /a
            Disallow: /b
        """.trimIndent()

        val policy = parser.parse(robotsTxt)

        // when & then
        assertFalse(policy.isAllowed("WebProbe", "/a"))
        assertFalse(policy.isAllowed("WebProbe", "/b"))
        assertTrue(policy.isAllowed("WebProbe", "/c"))
    }

    @Test
    fun `should fallback to allow when user-agent not defined`() {
        val robotsTxt = """
            User-agent: Googlebot
            Disallow: /google-only
        """.trimIndent()

        val policy = parser.parse(robotsTxt)

        assertFalse(policy.isAllowed("WebProbe", "google-only"))
    }
}