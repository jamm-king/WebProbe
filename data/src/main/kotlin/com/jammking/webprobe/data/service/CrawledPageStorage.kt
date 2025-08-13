package com.jammking.webprobe.data.service

import com.jammking.webprobe.data.entity.CrawledPage

interface CrawledPageStorage {
    fun save(url: String, title: String, html: String, text: String)
    fun findByUrl(url: String): CrawledPage?
    fun existsByUrl(url: String): Boolean
    fun deleteAll()
}