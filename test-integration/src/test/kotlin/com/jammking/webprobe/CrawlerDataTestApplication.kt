package com.jammking.webprobe

import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = [
    "com.jammking.webprobe.crawler",
    "com.jammking.webprobe.data"
])
class CrawlerDataTestApplication