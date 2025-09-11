package com.jammking.webprobe.data.service

import com.jammking.webprobe.data.entity.BlogPost
import com.jammking.webprobe.data.exception.StorageException
import com.jammking.webprobe.data.repository.BlogPostRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MongoBlogPostStorage(
    private val repository: BlogPostRepository
): BlogPostStorage {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun save(url: String, title: String, date: Instant, lang: String, text: String) {
        val post = BlogPost.of(url, title, date, lang, text)
        try {
            repository.save(post)
            log.info("Saved BlogPost: url=$url")
        } catch(e: Exception) {
            log.error("Failed to save BlogPost: url=$url, reason=${e.message}")
            throw StorageException("Failed to save BlogPost", e)
        }
    }

    override fun findByUrl(url: String): BlogPost? {
        return  try {
            repository.findByUrl(url).also {
                if(it != null) log.debug("Found BlogPost: url=$url")
                else log.debug("BlogPost not found: url=$url")
            }
        } catch(e: Exception) {
            log.error("Failed to find BlogPost: url=$url, reason=${e.message}")
            throw StorageException("Failed to find BlogPost", e)
        }
    }

    override fun existsByUrl(url: String): Boolean {
        return try {
            repository.existsById(url)
        } catch(e: Exception) {
            log.error("Failed to check BlogPost existence: url=$url, reason=${e.message}")
            throw StorageException("Failed to check BlogPost existence", e)
        }
    }

    override fun deleteAll() {
        try {
            repository.deleteAll()
            log.info("Deleted all BlogPost documents")
        } catch(e: Exception) {
            log.error("Failed to delete all BlogPost documents: reason=${e.message}")
            throw StorageException("Failed to delete all BlogPost records", e)
        }
    }
}