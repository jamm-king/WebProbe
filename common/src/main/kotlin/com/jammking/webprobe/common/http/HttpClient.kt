package com.jammking.webprobe.common.http

interface HttpClient {
    fun get(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse
}