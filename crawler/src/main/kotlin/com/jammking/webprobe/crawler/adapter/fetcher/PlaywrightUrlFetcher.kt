package com.jammking.webprobe.crawler.adapter.fetcher

import com.jammking.webprobe.crawler.exception.FetchFailedException
import com.jammking.webprobe.crawler.exception.ParseException
import com.jammking.webprobe.crawler.port.UrlFetcher
import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.PlaywrightException
import com.microsoft.playwright.TimeoutError
import com.microsoft.playwright.options.LoadState
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PlaywrightUrlFetcher(
    private val browser: Browser
) : UrlFetcher {

    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun fetch(url: String): Page {
        log.info("Playwright fetching: $url")

        val page = browser.newPage()

        return try {
            page.navigate(url)
            page.waitForLoadState(LoadState.NETWORKIDLE)
            page
        } catch (e: TimeoutError) {
            log.warn("Playwright timeout at $url", e)
            throw FetchFailedException(url, e)
        } catch (e: PlaywrightException) {
            log.warn("Playwright failure at $url", e)
            throw FetchFailedException(url, e)
        } catch (e: Exception) {
            log.warn("Unexpected parse failure at $url", e)
            throw ParseException(url, e.message ?: "Unknown error")
        }
    }
}