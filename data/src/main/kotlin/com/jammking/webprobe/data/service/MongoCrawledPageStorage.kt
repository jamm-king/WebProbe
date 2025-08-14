package com.jammking.webprobe.data.service

import com.jammking.webprobe.data.entity.CrawledPage
import com.jammking.webprobe.data.exception.StorageException
import com.jammking.webprobe.data.repository.CrawledPageRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MongoCrawledPageStorage(
    private val repository: CrawledPageRepository
): CrawledPageStorage {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun save(url: String, title: String, html: String, text: String) {
        val page = CrawledPage.of(url, title, html, text)
        try {
            repository.save(page)
            log.info("Saved CrawledPage: url=$url")
        } catch(e: Exception) {
            log.error("Failed to save CrawledPage: url=$url, reason=${e.message}")
            throw StorageException("Failed to save CrawledPage", e)
        }
    }

    override fun findByUrl(url: String): CrawledPage? {
        return  try {
            repository.findByUrl(url).also {
                if(it != null) log.debug("Found CrawledPage: url=$url")
                else log.debug("CrawledPage not found: url=$url")
            }
        } catch(e: Exception) {
            log.error("Failed to find CrawledPage: url=$url, reason=${e.message}")
            throw StorageException("Failed to find CrawledPage", e)
        }
    }

    override fun existsByUrl(url: String): Boolean {
        return try {
            repository.existsById(url)
        } catch(e: Exception) {
            log.error("Failed to check CrawledPage existence: url=$url, reason=${e.message}")
            throw StorageException("Failed to check CrawledPage existence", e)
        }
    }

    override fun deleteAll() {
        try {
            repository.deleteAll()
            log.info("Deleted all CrawledPage documents")
        } catch(e: Exception) {
            log.error("Failed to delete all CrawledPage documents: reason=${e.message}")
            throw StorageException("Failed to delete all CrawledPage records", e)
        }
    }
}