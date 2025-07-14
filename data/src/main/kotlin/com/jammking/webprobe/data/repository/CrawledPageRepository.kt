package com.jammking.webprobe.data.repository

import com.jammking.webprobe.data.entity.CrawledPage
import org.springframework.data.mongodb.repository.MongoRepository

interface CrawledPageRepository: MongoRepository<CrawledPage, String> {
    fun findByUrl(url: String): CrawledPage?
}