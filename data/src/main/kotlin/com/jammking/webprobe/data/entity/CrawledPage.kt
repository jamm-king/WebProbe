package com.jammking.webprobe.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Duration
import java.time.Instant

@Document(collection = "crawled_pages")
data class CrawledPage(
    @Id
    val url: String,
    val title: String,
    val html: String,
    val text: String,
    val summary: String?,
    val createdAt: Instant = Instant.now(),
    val expiredAt: Instant = createdAt.plus(Duration.ofDays(30))
)