package com.jammking.webprobe.crawler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.jammking.webprobe.crawler"])
class CrawlerTestApplication

fun main(args: Array<String>) {
    runApplication<CrawlerTestApplication>(*args)
}