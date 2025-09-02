package com.jammking.webprobe.data.repository

import com.jammking.webprobe.data.entity.BlogPost
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BlogPostRepository: MongoRepository<BlogPost, String> {
    fun findByUrl(url: String): BlogPost?
}