package com.jammking.webprobe.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebProbeApplication

fun main(args: Array<String>) {
    runApplication<WebProbeApplication>(*args)
}