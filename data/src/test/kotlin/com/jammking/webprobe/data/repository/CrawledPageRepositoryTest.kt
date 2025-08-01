package com.jammking.webprobe.data.repository

import com.jammking.webprobe.data.DataTestApplication
import com.jammking.webprobe.data.entity.CrawledPage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant

@ActiveProfiles("test")
@SpringBootTest(classes = [DataTestApplication::class])
class CrawledPageRepositoryTest {

    @Autowired
    lateinit var repository: CrawledPageRepository

    private val log = LoggerFactory.getLogger(this::class.java)

    @Test
    fun `should save and retrieve a crawled page by url`() {
        val url = "https://example.com"
        val crawledPage = CrawledPage(
            url = url,
            title = "Example Page",
            html = "<html><body>Hello</body></html>",
            text = "Hello",
            createdAt = Instant.now(),
        )

        log.info("Saving test CrawledPage for URL: $url")
        repository.save(crawledPage)

        val found = repository.findByUrl(url)

        log.info("Fetched page from DB: $found")

        assertThat(found).isNotNull
        assertThat(found!!.title).isEqualTo("Example Page")
    }
}