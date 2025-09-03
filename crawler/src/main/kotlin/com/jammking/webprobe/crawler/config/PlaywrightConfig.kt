package com.jammking.webprobe.crawler.config

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Playwright
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PlaywrightConfig {

    @Bean
    fun chromium(playwright: Playwright): Browser {
        return playwright.chromium().launch()
    }

    @Bean
    fun playwright(): Playwright {
        return Playwright.create()
    }
}