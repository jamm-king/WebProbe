package com.jammking.webprobe.crawler.adapter

import com.microsoft.playwright.Page
import com.microsoft.playwright.junit.UsePlaywright
import com.microsoft.playwright.options.LoadState
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import java.net.URLEncoder

@UsePlaywright
class PlaywrightTest {

    @Test
    fun `extract a tags' href attribute from tistory search page`(page: Page) {
        val keyword = "포항 식당 리뷰"
        val encoded = URLEncoder.encode(keyword, Charsets.UTF_8)
        val url = "https://www.tistory.com/search?keyword=$encoded&page=1"
        page.navigate(url)
        page.waitForLoadState(LoadState.NETWORKIDLE)

        val html = page.content()
        val doc = Jsoup.parse(html)
        val urls = doc.select("a.link_cont")
            .mapNotNull { it.attr("href") }
            .filter { it.startsWith("http") }

        urls.forEach {
            println(it)
        }
    }

    @Test
    fun `extract data from tistory post`(page: Page) {
//        page.navigate("https://goodprogramer.tistory.com/359")
        page.navigate("https://utokia.tistory.com/204")

        val title = page.locator(".title-article").textContent()
        val date = page.locator(".date").allTextContents()[0]
        val lang = page.locator("html").getAttribute("lang")
        val text = page.locator(".article-view").locator("span").allTextContents().joinToString("\n")

        println("title : $title")
        println("date : $date")
        println("lang : $lang")
        println("text : $text")
    }
}