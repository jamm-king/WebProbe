package com.jammking.webprobe.data.repository

import com.jammking.webprobe.data.DataTestApplication
import com.jammking.webprobe.data.entity.BlogPost
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant

@ActiveProfiles("test")
@SpringBootTest(classes = [DataTestApplication::class])
class BlogPostRepositoryTest {

    @Autowired
    lateinit var repository: BlogPostRepository

    private val log = LoggerFactory.getLogger(this::class.java)

    @Test
    fun `should save and retrieve a blog post by url`() {
        val url = "https://example.com"
        val blogPost = BlogPost.of(
            url = url,
            title = "Example Post",
            date = Instant.now(),
            lang = "ko",
            text = "Hello",
        )

        log.info("Saving test blog post for URL: $url")
        repository.save(blogPost)

        val found = repository.findByUrl(url)

        log.info("Fetched post from DB: $found")

        assertThat(found).isNotNull
        assertThat(found!!.title).isEqualTo("Example Post")
    }
}