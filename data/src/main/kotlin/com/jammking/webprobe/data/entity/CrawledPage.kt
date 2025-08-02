package com.jammking.webprobe.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "crawled_pages")
data class CrawledPage(
    @Id
    val url: String,
    val title: String,
    val html: String,
    val text: String,
    @Indexed(expireAfterSeconds = 60 * 60 * 24 * 30)
    val createdAt: Instant = Instant.now(),
) {
    companion object {
        fun of(
            url: String,
            title: String,
            html: String,
            text: String
        ): CrawledPage {
            return CrawledPage(
                url = url,
                title = title,
                html = html,
                text = text
            )
        }
    }
}