package com.jammking.webprobe.data.repository

import com.jammking.webprobe.data.entity.CrawledPage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import java.time.Duration
import java.time.Instant

@DataMongoTest
class CrawledPageRepositoryTest {

    @Autowired
    lateinit var repository: CrawledPageRepository

    private val log = LoggerFactory.getLogger(this::class.java)

    @Test
    fun `should save and retrieve a crawled page by url`() {
        val url = "https://example.com"
        val page = CrawledPage(
            url = url,
            title = "Example Page",
            html = "<html><body>Hello</body></html>",
            text = "Hello",
            summary = "Short summary",
            createdAt = Instant.now(),
            expiredAt = Instant.now().plus(Duration.ofDays(30))
        )

        log.info("Saving test CrawledPage for URL: $url")
        repository.save(page)

        val found = repository.findByUrl(url)

        log.info("Fetched page from DB: $found")

        assertThat(found).isNotNull
        assertThat(found!!.title).isEqualTo("Example Page")
    }
}