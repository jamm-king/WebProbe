package com.jammking.webprobe.application.controller

import com.jammking.webprobe.application.exception.InvalidSearchRequestException
import com.jammking.webprobe.crawler.model.CrawlerResult
import com.jammking.webprobe.crawler.model.SearchRequest
import com.jammking.webprobe.crawler.service.Crawler
import kotlinx.coroutines.reactor.mono
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/search")
class CrawlerController(
    private val crawler: Crawler
) {

    @PostMapping
    fun search(@RequestBody request: SearchRequest): Mono<ResponseEntity<CrawlerResult>> = mono {
        validateRequest(request)
        val result = crawler.crawl(request)
        ResponseEntity.ok(result)
    }

    private fun validateRequest(request: SearchRequest) {
        if(request.keyword.isBlank()) {
            throw InvalidSearchRequestException("keyword must not be blank")
        }

        if(request.userId == null && request.fresh) {
            throw InvalidSearchRequestException("fresh=true is only allowed when userId id provided")
        }
    }
}