package com.jammking.webprobe.data.service

import com.jammking.webprobe.data.DataTestApplication
import com.jammking.webprobe.data.repository.CrawledPageRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(classes = [DataTestApplication::class])
class MongoCrawledPageStorageTest {

    @Autowired
    lateinit var repository: CrawledPageRepository

    @Autowired
    lateinit var storage: CrawledPageStorage

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `should save and retrieve CrawledPage by url`() {
        // given
        val url = "https://example.com"
        val title = "Example Title"
        val html = "<html><body>Hello</body></html>"
        val text = "hello"

        // when
        storage.save(url, title, html, text)

        // then
        val retrieved = storage.findByUrl(url)
        assertNotNull(retrieved, "CrawledPage should be found")
        assertEquals(title, retrieved?.title)
        assertEquals(html, retrieved?.html)
        assertEquals(text, retrieved?.text)
    }

    @Test
    fun `should return true if CrawledPage exists`() {
        // given
        val url = "https://example.com"
        storage.save(url, "Title", "<html>HTML</html>", "Text")

        // expect
        val exists = storage.existsByUrl(url)
        assertTrue(exists, "Crawled should exist")
    }

    @Test
    fun `should return false if CrawledPage does not exist`() {
        // given
        val url = "https://example.com"

        // expect
        val exists = storage.existsByUrl(url)
        assertFalse(exists, "CrawledPage should not exist")
    }
}