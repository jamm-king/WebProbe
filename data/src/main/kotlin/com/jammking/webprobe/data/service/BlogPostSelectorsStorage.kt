package com.jammking.webprobe.data.service

import com.jammking.webprobe.common.constants.Domain
import com.jammking.webprobe.data.entity.BlogPostSelectors

interface BlogPostSelectorsStorage {
    fun save(titleSel: String, dateSel: String, contentSel: String, domain: Domain)
    fun findByDomain(domain: Domain): List<BlogPostSelectors>
    fun deleteAll()
}