package com.jammking.webprobe.data.repository

import com.jammking.webprobe.common.constants.Domain
import com.jammking.webprobe.data.DataTestApplication
import com.jammking.webprobe.data.entity.BlogPostSelectors
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles


@ActiveProfiles("test")
@SpringBootTest(classes = [DataTestApplication::class])
class BlogPostSelectorsRepositoryTest {

    @Autowired
    lateinit var repository: BlogPostSelectorsRepository

    private val log = LoggerFactory.getLogger(this::class.java)

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `should save and retrieve blog post selectors list by domain`() {
        // given
        val id = "TISTORY_1"
        val titleSel = ".title"
        val dateSel = ".date"
        val contentSel = ".content"
        val domain = Domain.TISTORY

        val selectors = BlogPostSelectors.of(
            id = id,
            titleSel = titleSel,
            dateSel = dateSel,
            contentSel = contentSel,
            domain = domain
        )
        repository.save(selectors)

        // when
        val result = repository.findByDomain(domain)

        // then
        assertEquals(1, result.size)
        assertEquals(id, result.first().id)
        assertEquals(titleSel, result.first().titleSel)
        assertEquals(dateSel, result.first().dateSel)
        assertEquals(contentSel, result.first().contentSel)
        assertEquals(domain, result.first().domain)
    }
}