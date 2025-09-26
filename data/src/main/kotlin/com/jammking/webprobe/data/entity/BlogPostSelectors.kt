package com.jammking.webprobe.data.entity

import com.jammking.webprobe.common.constants.Domain
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "blog_post_selectors")
data class BlogPostSelectors(
    @Id var id: String,
    val titleSel: String,
    val dateSel: String,
    val contentSel: String,
    @Indexed(name = "domain_idx")
    val domain: Domain,
    @Indexed(expireAfterSeconds = 60 * 60 * 24 * 30)
    val recentlyUsedAt: Instant = Instant.now()
) {
    companion object {
        fun of(
            id: String,
            titleSel: String,
            dateSel: String,
            contentSel: String,
            domain: Domain
        ): BlogPostSelectors {
            return BlogPostSelectors(
                id = id,
                titleSel = titleSel,
                dateSel = dateSel,
                contentSel = contentSel,
                domain = domain
            )
        }
    }
}
