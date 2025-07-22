package com.jammking.webprobe.crawler.adapter.fetcher

import com.jammking.webprobe.crawler.exception.FetchFailedException
import com.jammking.webprobe.crawler.exception.ParseException
import com.jammking.webprobe.crawler.model.CrawledPage
import com.jammking.webprobe.crawler.port.UrlFetcher
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.PlaywrightException
import com.microsoft.playwright.TimeoutError
import com.microsoft.playwright.options.LoadState
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PlaywrightUrlFetcher : UrlFetcher {

    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun fetch(url: String): CrawledPage {
        log.info("Playwright fetching: $url")

        val playwright = try {
            Playwright.create()
        } catch(e: Exception) {
            throw FetchFailedException(url, e)
        }

        val browser = playwright.chromium().launch()
        val page = browser.newPage()

        return try {
            page.navigate(url)
            page.waitForLoadState(LoadState.NETWORKIDLE)

            val title = page.title()
            val html = page.content()
            val text = page.locator("body").textContent() ?: ""

            CrawledPage(
                url = url,
                title = title,
                html = html,
                text = text
            )
        } catch (e: TimeoutError) {
            log.warn("Playwright timeout at $url", e)
            throw FetchFailedException(url, e)
        } catch (e: PlaywrightException) {
            log.warn("Playwright failure at $url", e)
            throw FetchFailedException(url, e)
        } catch (e: Exception) {
            log.warn("Unexpected parse failure at $url", e)
            throw ParseException(url, e.message ?: "Unknown error")
        } finally {
            try {
                browser.close()
                playwright.close()
            } catch (e: Exception) {
                log.warn("Error while closing Playwright f or $url", e)
            }
        }
    }
}