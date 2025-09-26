package com.jammking.webprobe.data.service

import com.jammking.webprobe.common.constants.Domain
import com.jammking.webprobe.data.entity.BlogPostSelectors
import com.jammking.webprobe.data.exception.StorageException
import com.jammking.webprobe.data.repository.BlogPostSelectorsRepository
import com.mongodb.DuplicateKeyException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MongoBlogPostSelectorsStorage(
    private val repository: BlogPostSelectorsRepository,
    private val seq: DomainSequenceService
): BlogPostSelectorsStorage {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun save(titleSel: String, dateSel: String, contentSel: String, domain: Domain) {
        val id = seq.nextId(domain)
        val post = BlogPostSelectors.of(id, titleSel, dateSel, contentSel, domain)
        try {
            repository.save(post)
            log.info("Saved BlogPostSelectors: id=$id")
        } catch(e: DuplicateKeyException) {
            log.warn("Duplicate id detected (retrying): id=$id")
            val retryId = seq.nextId(domain)
            repository.save(post.copy(id = retryId))
        } catch(e: Exception) {
            log.error("Failed to save BlogPostSelectors: id=$id, reason=${e.message}")
            throw StorageException("Failed to save BlogPostSelectors", e)
        }
    }

    override fun findByDomain(domain: Domain): List<BlogPostSelectors> {
        return  try {
            repository.findByDomain(domain).also {
                if(it.isNotEmpty()) log.debug("Found BlogPostSelectors: domain={}", domain)
                else log.debug("BlogPostSelectors not found: domain={}", domain)
            }
        } catch(e: Exception) {
            log.error("Failed to find BlogPostSelectors: domain=$domain, reason=${e.message}")
            throw StorageException("Failed to find BlogPostSelectors", e)
        }
    }

    override fun deleteAll() {
        try {
            repository.deleteAll()
            log.info("Deleted all BlogPostSelectors documents")
        } catch(e: Exception) {
            log.error("Failed to delete all BlogPostSelectors documents: reason=${e.message}")
            throw StorageException("Failed to delete all BlogPostSelectors documents", e)
        }
    }

}