package com.jammking.webprobe.data.repository

import com.jammking.webprobe.data.DataTestApplication
import com.jammking.webprobe.data.entity.UserSeenUrl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(classes = [DataTestApplication::class])
class UserSeenUrlRepositoryTest {

    @Autowired
    lateinit var repository: UserSeenUrlRepository

    private val userId = "test-user"

    @BeforeEach
    fun clean() {
        repository.deleteAllByUserId(userId)
    }

    @Test
    fun `should retrieve a seen url by userId and url`() {
        // given
        val url = "https://example.com"

        val saved = repository.save(UserSeenUrl(userId = userId, url = url))

        // when
        val found = repository.findByUserIdAndUrl(userId, url)

        // then
        assertThat(found).isNotNull
        assertThat(found!!.url).isEqualTo(url)
        assertThat(found.userId).isEqualTo(userId)
    }

    @Test
    fun `should retrieve all urls seen by a user`() {
        // given
        val urls = listOf("https://a.com", "https://b.com")

        val saved = repository.saveAll(urls.map { UserSeenUrl(userId = userId, url = it) })

        // when
        val results = repository.findAllByUserId(userId)

        // then
        assertThat(results).hasSize(2)
        assertThat(results.map { it.url }).containsAll(urls)
    }

    @Test
    fun `should delete all entries by userId`() {
        // given
        repository.save(UserSeenUrl(userId = userId, url = "https://x.com"))

        // when
        repository.deleteAllByUserId(userId)

        // then
        val result = repository.findAllByUserId(userId)
        assertThat(result).isEmpty()
    }
}