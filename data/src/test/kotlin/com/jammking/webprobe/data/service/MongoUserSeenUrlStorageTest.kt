package com.jammking.webprobe.data.service

import com.jammking.webprobe.data.DataTestApplication
import com.jammking.webprobe.data.repository.UserSeenUrlRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(classes = [DataTestApplication::class])
class MongoUserSeenUrlStorageTest {

    @Autowired
    lateinit var userSeenUrlRepository: UserSeenUrlRepository

    @Autowired
    lateinit var userSeenStorage: UserSeenStorage

    @BeforeEach
    fun setup() {
        userSeenUrlRepository.deleteAll()
    }

    @Test
    fun `should save and check if url is seen for user`() {
        // given
        val userId = "user-123"
        val url = "https://example.com"

        // when
        userSeenStorage.save(userId, url)

        // then
        val isSeen = userSeenStorage.isSeen(userId, url)
        assertTrue(isSeen, "URL should be marked as seen for the user")
    }

    @Test
    fun `should return false if url is not seen for user`() {
        // given
        val userId = "user-456"
        val url = "https://example.com/not-seen"

        // when
        // nothing

        // then
        val isSeen = userSeenStorage.isSeen(userId, url)
        assertFalse(isSeen, "URL should not be marked as seen for the user")
    }

    @Test
    fun `should isolate seen urls per user`() {
        // given
        val url = "https://example.com/shared"
        val userA = "user-a"
        val userB = "user-b"

        // when
        userSeenStorage.save(userA, url)

        // then
        assertTrue(userSeenStorage.isSeen(userA, url), "User A should have seen the URL")
        assertFalse(userSeenStorage.isSeen(userB, url), "User B should not have seen the URL")
    }
}