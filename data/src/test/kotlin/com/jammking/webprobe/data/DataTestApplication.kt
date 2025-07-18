package com.jammking.webprobe.data

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.jammking.webprobe.data"])
class DataTestApplication

fun main(args: Array<String>) {
    runApplication<DataTestApplication>(*args)
}