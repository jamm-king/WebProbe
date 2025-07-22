package com.jammking.webprobe.crawler.service.resolver

import com.jammking.webprobe.crawler.port.UrlFetcher
import com.jammking.webprobe.crawler.adapter.fetcher.DefaultUrlFetcher
import com.jammking.webprobe.crawler.adapter.fetcher.PlaywrightUrlFetcher
import org.springframework.stereotype.Component

@Component
class UrlFetcherResolver(
    private val defaultUrlFetcher: DefaultUrlFetcher,
    private val playwrightUrlFetcher: PlaywrightUrlFetcher
) {
    fun resolve(url: String): UrlFetcher {
        return if(requiresJs(url)) playwrightUrlFetcher else defaultUrlFetcher
    }

    private fun requiresJs(url: String): Boolean {
        return listOf("blog.naver.com").any { url.contains(it) }
    }
}