package com.jammking.webprobe.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("user_seen_urls")
@CompoundIndex(name = "user_url_idx", def = "{'userId': 1, 'url': 1}", unique = true)
data class UserSeenUrl(
    @Id
    val id: String = Object().toString(),
    val userId: String,
    val url: String,
    @Indexed(expireAfterSeconds = 60 * 60 * 24 * 30)
    val seenAt: Instant = Instant.now()
)
