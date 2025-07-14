package com.jammking.webprobe.data.config

import com.jammking.webprobe.data.entity.CrawledPage
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index

@Configuration
class MongoConfig(
    private val mongoTemplate: MongoTemplate
) {
    @PostConstruct
    fun setupIndexes() {
        val ttlIndex = Index()
            .on("expiredAt", Sort.Direction.ASC)
            .expire(0)

        mongoTemplate.indexOps(CrawledPage::class.java).ensureIndex(ttlIndex)
    }
}