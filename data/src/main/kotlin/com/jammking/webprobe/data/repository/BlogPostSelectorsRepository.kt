package com.jammking.webprobe.data.repository

import com.jammking.webprobe.common.constants.Domain
import com.jammking.webprobe.data.entity.BlogPostSelectors
import org.springframework.data.mongodb.repository.MongoRepository

interface BlogPostSelectorsRepository: MongoRepository<BlogPostSelectors, String> {
    fun findByDomain(domain: Domain): List<BlogPostSelectors>
}