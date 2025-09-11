package com.jammking.webprobe.data.service

import com.jammking.webprobe.data.entity.BlogPost
import java.time.Instant

interface BlogPostStorage {
    fun save(url: String, title: String, date: Instant, lang: String, text: String)
    fun findByUrl(url: String): BlogPost?
    fun existsByUrl(url: String): Boolean
    fun deleteAll()
}