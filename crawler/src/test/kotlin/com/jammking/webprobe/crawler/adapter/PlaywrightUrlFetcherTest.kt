package com.jammking.webprobe.crawler.adapter

import com.jammking.webprobe.crawler.CrawlerTestApplication
import com.jammking.webprobe.crawler.adapter.fetcher.PlaywrightUrlFetcher
import com.jammking.webprobe.data.entity.CrawledPage
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [CrawlerTestApplication::class])
class PlaywrightUrlFetcherTest {

    @Autowired
    lateinit var fetcher: PlaywrightUrlFetcher

    @Test
    fun shouldFetchFromPage(): Unit = runBlocking {
        // given
        val url = "https://blog.naver.com/PostView.nhn?blogId=biinii_ary&logNo=223549318498&redirect=Dlog&widgetTypeCall=true"

        // when
        val result: CrawledPage = fetcher.fetch(url)

        // then
        assertThat(result.url).isEqualTo(url)
        assertThat(result.title).isEqualTo("포항 맛집 모음 포항 찐맛집 8곳 총정리 내돈내먹 포항여행맛집코스 : 네이버 블로그")
        assertThat(result.html).contains("<html").contains("</html>")
        assertThat(result.text).isNotBlank()
    }
}