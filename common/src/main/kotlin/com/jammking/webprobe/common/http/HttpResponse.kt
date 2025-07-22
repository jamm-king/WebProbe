package com.jammking.webprobe.common.http

data class HttpResponse(
    val statusCode: Int,
    val body: String?,
    val headers: Map<String, List<String>>
)
