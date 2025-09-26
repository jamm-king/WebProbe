package com.jammking.webprobe.data.service

import com.jammking.webprobe.common.constants.Domain
import com.jammking.webprobe.data.entity.BlogPostSelectorsCounter
import com.jammking.webprobe.data.exception.StorageException
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

@Service
class DomainSequenceService(
    private val mongoOps: MongoOperations
) {
    fun nextId(domain: Domain): String {
        val q = Query(Criteria.where("_id").`is`(domain.name))
        val u = Update().inc("seq", 1)
        val opts = FindAndModifyOptions.options().returnNew(true).upsert(true)
        val c = mongoOps.findAndModify(q, u, opts, BlogPostSelectorsCounter::class.java)
            ?: throw StorageException("Failed to update counter for ${domain.name}")

        return "${domain.name}_${c.seq}"
    }
}