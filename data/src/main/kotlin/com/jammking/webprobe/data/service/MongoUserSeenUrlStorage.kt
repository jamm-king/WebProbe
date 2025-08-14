package com.jammking.webprobe.data.service

import com.jammking.webprobe.data.entity.UserSeenUrl
import com.jammking.webprobe.data.exception.StorageException
import com.jammking.webprobe.data.repository.UserSeenUrlRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MongoUserSeenUrlStorage(
    private val repository: UserSeenUrlRepository
): UserSeenStorage {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun save(userId: String, url: String) {
        val userSeenUrl = UserSeenUrl.of(userId, url)
        try {
            repository.save(userSeenUrl)
            log.info("Saved UserSeenUrl: userId=$userId, url=$url")
        } catch(e: Exception) {
            log.warn("Failed to save UserSeenUrl (possible duplicate): userId=$userId, reason=${e.message}")
        }
    }

    override fun isSeen(userId: String, url: String): Boolean {
        return try {
            repository.existsByUserIdAndUrl(userId, url)
        } catch(e: Exception) {
            log.error("Failed to check UserSeenUrl existence: userId=$userId, url=$url, reason=${e.message}")
            throw StorageException("Failed to check UserSeenUrl existence", e)
        }
    }

    override fun deleteAll() {
        try {
            repository.deleteAll()
            log.info("Deleted all UserSeenUrl documents")
        } catch(e: Exception) {
            log.error("Failed to delete all UserSeenUrl documents: reason=${e.message}")
            throw StorageException("Failed to delete all UserSeenUrl records", e)
        }
    }
}