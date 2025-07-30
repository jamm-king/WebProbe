package com.jammking.webprobe.data.repository

import com.jammking.webprobe.data.entity.Page
import org.springframework.data.mongodb.repository.MongoRepository

interface PageRepository: MongoRepository<Page, String> {
    fun findByUrl(url: String): Page?
}