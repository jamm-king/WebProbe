package com.jammking.webprobe

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = [
    "com.jammking.webprobe.crawler",
    "com.jammking.webprobe.data"
])
class CrawlerDataTestApplication

fun main(args: Array<String>) {
    runApplication<CrawlerDataTestApplication>(*args)
}