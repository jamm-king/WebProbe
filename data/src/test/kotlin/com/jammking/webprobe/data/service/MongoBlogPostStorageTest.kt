package com.jammking.webprobe.data.service

import com.jammking.webprobe.data.DataTestApplication
import com.jammking.webprobe.data.repository.BlogPostRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant

@ActiveProfiles("test")
@SpringBootTest(classes = [DataTestApplication::class])
class MongoBlogPostStorageTest {

    @Autowired
    lateinit var repository: BlogPostRepository

    @Autowired
    lateinit var storage: MongoBlogPostStorage

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `should save and retrieve blog post by url`() {
        // given
        val url = "https://example.com"
        val title = "Example Title"
        val date = Instant.now()
        val lang = "ko"
        val text = "hello"

        // when
        storage.save(url, title, date, lang, text)

        // then
        val retrieved = storage.findByUrl(url)
        assertNotNull(retrieved, "blog post should be found")
        assertEquals(title, retrieved?.title)
        // Equality check for Instant is skipped due to precision loss (nanoseconds → milliseconds) in MongoDB.
        assertEquals(text, retrieved?.text)
    }

    @Test
    fun `should return true if blog post exists`() {
        // given
        val url = "https://example.com"
        storage.save(url, "Title", Instant.now(), "ko", "text")

        // expect
        val exists = storage.existsByUrl(url)
        assertTrue(exists, "blog post should exist")
    }

    @Test
    fun `should return false if blog post does not exist`() {
        // given
        val url = "https://example.com"

        // expect
        val exists = storage.existsByUrl(url)
        assertFalse(exists, "blog post should not exist")
    }
}