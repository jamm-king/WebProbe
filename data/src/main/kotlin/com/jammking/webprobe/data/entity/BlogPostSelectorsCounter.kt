package com.jammking.webprobe.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("blog_post_selectors_counters")
data class BlogPostSelectorsCounter(
    @Id val id: String,
    var seq: Long = 0
)
