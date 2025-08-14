package com.jammking.webprobe.data.service

import com.jammking.webprobe.CrawlerDataTestApplication
import com.jammking.webprobe.data.entity.UserSeenUrl
import com.jammking.webprobe.data.repository.UserSeenUrlRepository
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
class MongoUserSeenUrlStorageTest(
    @Autowired private val storage: MongoUserSeenUrlStorage,
    @Autowired private val repository: UserSeenUrlRepository
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
    fun `should remove all documents`() {
        // given
        repository.save(UserSeenUrl.of("user1", "https://example.com/1"))
        repository.save(UserSeenUrl.of("user2", "https://example.com/2"))
        assertThat(repository.count()).isEqualTo(2)

        // when
        storage.deleteAll()

        // then
        assertThat(repository.count()).isEqualTo(0)
    }
}