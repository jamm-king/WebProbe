package com.jammking.webprobe.crawler.config

import com.jammking.webprobe.common.http.HttpClient
import com.jammking.webprobe.common.http.OkHttpClientWrapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HttpClientConfig {

    @Bean
    fun httpClient(): HttpClient = OkHttpClientWrapper()
}