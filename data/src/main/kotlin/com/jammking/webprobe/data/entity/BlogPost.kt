package com.jammking.webprobe.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "blog_posts")
data class BlogPost(
    @Id val url: String,
    val title: String,
    val date: Instant,
    val lang: String,
    val text: String,
    val wordCount: Int,
    @Indexed(expireAfterSeconds = 60 * 60 * 24 * 30)
    val createdAt: Instant = Instant.now()
) {
    companion object {
        fun of(
            url: String,
            title: String,
            date: Instant,
            lang: String,
            text: String,
        ): BlogPost {
            return BlogPost(
                url = url,
                title = title,
                date = date,
                lang = lang,
                text = normalize(text),
                wordCount = wordCount(text)
            )
        }

        private fun normalize(text: String): String =
            text.lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .joinToString("\n")

        private fun wordCount(text: String): Int =
            text.split(Regex("\\s+")).count { it.isNotBlank() }
    }
}