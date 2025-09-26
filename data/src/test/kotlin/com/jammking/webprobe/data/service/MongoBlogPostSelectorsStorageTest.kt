package com.jammking.webprobe.data.service

import com.jammking.webprobe.common.constants.Domain
import com.jammking.webprobe.data.DataTestApplication
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(classes = [DataTestApplication::class])
class MongoBlogPostSelectorsStorageTest {

    @Autowired
    lateinit var service: MongoBlogPostSelectorsStorage

    private val log = LoggerFactory.getLogger(this::class.java)

    @BeforeEach
    fun setup() {
        service.deleteAll()
    }

    @Test
    fun `should save and retrieve selectors by domain`() {
        // given
        val titleSel = ".title"
        val dateSel = ".date"
        val contentSel = ".content"
        val domain = Domain.TISTORY

        // when
        service.save(titleSel, dateSel, contentSel, domain)

        // then
        val result = service.findByDomain(domain)
        assertEquals(1, result.size)
        assertEquals(titleSel, result.first().titleSel)
        assertEquals(dateSel, result.first().dateSel)
        assertEquals(contentSel, result.first().contentSel)
        assertEquals(domain, result.first().domain)
    }

    @Test
    fun `should create id properly when save`() {
        // given
        val titleSel1 = ".title"
        val dateSel1 = ".date"
        val contentSel1 = ".content"

        val titleSel2 = ".article-title"
        val dateSel2 = ".article-date"
        val contentSel2 = ".article-content"

        val domain = Domain.TISTORY

        // when
        service.save(titleSel1, dateSel1, contentSel1, domain)
        service.save(titleSel2, dateSel2, contentSel2, domain)

        // then
        val result = service.findByDomain(domain)
        assertEquals(2, result.size)
        assertEquals("TISTORY_1", result.first().id)
        assertEquals("TISTORY_2", result.last().id)
    }

    @Test
    fun `should create id properly after deletion`() {
        // given
        val titleSel1 = ".title"
        val dateSel1 = ".date"
        val contentSel1 = ".content"

        val titleSel2 = ".article-title"
        val dateSel2 = ".article-date"
        val contentSel2 = ".article-content"

        val domain = Domain.TISTORY

        // when
        service.save(titleSel1, dateSel1, contentSel1, domain)
        service.save(titleSel2, dateSel2, contentSel2, domain)
        service.deleteAll()
        service.save(titleSel1, dateSel1, contentSel1, domain)
        service.save(titleSel2, dateSel2, contentSel2, domain)

        // then
        val result = service.findByDomain(domain)
        assertEquals(2, result.size)
        assertEquals("TISTORY_3", result.first().id)
        assertEquals("TISTORY_4", result.last().id)
    }
}