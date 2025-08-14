package com.jammking.webprobe.data.service

import com.jammking.webprobe.CrawlerDataTestApplication
import com.jammking.webprobe.data.entity.CrawledPage
import com.jammking.webprobe.data.repository.CrawledPageRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Tag("integration")
@ActiveProfiles("integration")
@SpringBootTest(classes = [CrawlerDataTestApplication::class])
class MongoCrawledPageStorageTest(
    @Autowired private val storage: MongoCrawledPageStorage,
    @Autowired private val repository: CrawledPageRepository
) {

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @AfterEach
    fun cleanup() {
        repository.deleteAll()
    }

    @Test
    fun `should remove all records`() {
        // given
        repository.save(CrawledPage.of("https://example.com/1", "Title1", "<html>1</html>", "Text1"))
        repository.save(CrawledPage.of("https://example.com/2", "Title2", "<html>2</html>", "Text2"))
        assertThat(repository.count()).isEqualTo(2)

        // when
        storage.deleteAll()

        // then
        assertThat(repository.count()).isEqualTo(0)
    }
}