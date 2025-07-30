package com.jammking.webprobe.crawler.config

import com.jammking.webprobe.crawler.adapter.searcher.TistorySearcher
import com.jammking.webprobe.crawler.model.SearchEngine
import com.jammking.webprobe.crawler.port.Searcher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SearcherConfig {

    @Bean
    fun searcherMap(
        tistorySearcher: TistorySearcher
    ): Map<SearchEngine, Searcher> = mapOf(
        SearchEngine.TISTORY to tistorySearcher
    )
}